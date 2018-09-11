FROM navikt/java:8

COPY build/install/kafka-postnummer/bin/kafka-postnummer bin/app
COPY build/install/kafka-postnummer/lib ./lib/
