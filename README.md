Kafka Postnummer-service
========================

Denne eksempelapplikasjonen forsøker å vise hvordan vi kan bruke 
Kafka til å lage en postnummertjeneste som gir deg tilhørende poststed.
Dataene kommer rett fra Postens postnummerregister-fil, og skrives til Kafka linje-for-linje. 
Deretter blir de transformert i en Kafka-strøm og gruppert slik at vi kan gjøre oppslag basert på postnummer.

## Komme i gang

Først av alt må du finne ut om du ønsker å bruke et lokalt Kafka-kluster
eller om du skal bruke NAV sitt eget. Det enkleste er selvsagt et lokalt kluster; der opprettes
topics automatisk og du slipper å tenke på tilgangsstyring.

### Installer lokal Kafka (OSX)

```
$ brew install kafka
$ vi /usr/local/etc/kafka/server.properties
    
    # add the following lines:
    listeners=PLAINTEXT://localhost:9092
    advertised.listeners=PLAINTEXT://localhost:9092
```


Start zookeeper og kafka:
```
$ zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties
$ kafka-server-start /usr/local/etc/kafka/server.properties
```

### Kafka i NAV

Dersom du ønsker å bruke NAV sitt Kafka-kluster så må du først opprette topicen:

```
curl -v -X PUT \
    -H 'Content-type: application/json' \
    -u $USER -p \
    https://kafka-adminrest.nais.adeo.no/api/v1/oneshot \
    -d @- << EOF
{
    "topics":[{
        "topicName":"postnummer",
        "numPartitions":1,
        "configEntries":{
            "retention.ms":"-1",
            "cleanup.policy":"compact"
        },
        "members":[{
            "member":"srvpostnummer",
            "role":"PRODUCER"
        },{
            "member": "srvpostnummer",
            "role":"CONSUMER"
        }]
    }]
}
EOF
```

### Produsere postnummer på topic

Hvordan dataene kommer inn på topic er ikke så viktig, men følgende eksempel
er desidert den enkleste måten:

```
curl -L https://www.bring.no/radgivning/sende-noe/adressetjenester/postnummer/postnummertabeller-veiledning/_/attachment/download/7f0186f6-cf90-4657-8b5b-70707abeb789:676b821de9cff02aaa7a009daf0af8a2a346a1bc/Postnummerregister-ansi.txt \
    | iconv -f windows-1252 -t utf-8 \
    | kafka-console-producer --broker-list localhost:9092 --topic postnummer
```

Denne kommandoen kan for eksempel kjøres regelmessig slik at postnummertjenesten
vår alltid er oppdatert med Postens endringer.

### Bygge applikasjonen

```
./gradlew assemble
```

### Kjøre applikasjonen

```
./gradlew run
```

### Teste

```
$ curl http://localhost:8080/postnummer/0354 | jq
{
  "postnummer": "0354",
  "poststed": "OSLO",
  "kommuneNr": "0301",
  "kommune": "OSLO",
  "type": "G"
}
```
