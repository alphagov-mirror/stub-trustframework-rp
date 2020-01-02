# stub-trustframework-rp

Stub Trust Framework RP acts as a service that can consume identities from a Broker on a particular scheme.  

### To use stub-trustframework-rp
* There can be 2 separate instances of this RP run at the same time, to replicate 2 different services. RP-1 can be located at http://localhost:4410/ whilst RP-1 is located at http://localhost:5510/
* Clone the [Stub OIDC Broker](https://github.com/alphagov/stub-oidc-broker) and follow the instructions on the README. 
* Alternatively there are 2 start-up scripts for each RP if you wish to run the RP's individually and not with the rest of the Trust Framework prototype. 

### Stub Trust framework RP runs on the PAAS
* To deploy Stub Trust framework RP login to the PAAS and select the build-learn space. 
* Run './gradlew pushToPaas' and this will deploy the app.
* RP-1 can be located on the PAAS at https://stub-trustframework-rp-1.cloudapps.digital/ whilst RP-2 can be located on the PAAS at https://stub-trustframework-rp-2.cloudapps.digital/.
