# stub-trustframework-rp

Stub Trust framework RP acts as a service that can consume identities from a Broker.  

You can find the Stub OpenID Broker [here](https://github.com/alphagov/stub-oidc-broker)
You can find the Stub OpenID Connect Provider [here](https://github.com/alphagov/stub-oidc-op)

### To use stub-oidc-broker
* Ensure you have [Stub OIDC OP](https://github.com/alphagov/stub-oidc-op) and [Stub OIDC Broker](https://github.com/alphagov/stub-oidc-broker) up and running
* Run startup.sh
* Go to http://localhost:4410/ in your browser and click Send request

### Stub Trust framework RP runs on the PAAS 
* To deploy Stub Trust framework RP simply login to the PAAS and select the build-learn space. 
* Run './gradlew pushToPaas' and this will deploy the app.