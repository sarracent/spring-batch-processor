# MC PROCESOR


### Flujo:

- El procesador se contectara al SFTP en busca de nuevos archivos. En el caso de encontrarlos los movera al directorio local "encrypted"
- Los archivos en el directorio "encripted" se desencriptaran y verificara la firma, dejando el resultado en el directorio "decrypted"
- Finalmente el job tomara el archivo desencriptado y ejecutara el procesamiento del archivo invocando primero topic service para obtener id de agencia y luego a la API CORE de Multichannel con un post para agregar registros a agency_contact


### Resources
Para el correcto funcionamiento se debe contar con los siguientes recursos:

- Llave privada de sondeos "secretSondeos.asc" (Desencriptar)
- Llave publica del cliente, en este caso BBVA "publicBBVA.asc" (Verificar firma)
- Llave Publica de sondeos "publicSondeos.asc" (Para enviar al cliente en el caso de que tengamos que nosotros enviar archivos encriptados)


### Config

spring.batch.job.enabled=false  
_(Inhabilita la ejecucion del job al iniciar el procesador)_ 

pgp.publicKeyFile = localpath/publicBBVA.asc  
_(Path a la llave publica del BBVA)_  

pgp.secretKeyFile = localpath/secretSondeos.asc  
_(Path a la llave privada de Sondeos)_  

pgp.passphrase = 321pepe321  
(Clave para llave privada de Sondeos)

decrypted.directory = decrypted  
(path donde se pondrán los archivos desencriptados)  

services.topics.url= http://dominio.produccion/topic-service/topics/  
(URL hacia la API de topic service)  

services.api.url= https://dominio.produccion/agency-contact/  
(URL hacia la API CORE de Multichannel)  

company.id=1  
(ID de la compañia correspondiente al BBVA)  



#### remote host properties

sftp.host.ip = 192.168.10.74  
sftp.host.port = 22  
sftp.host.user = root  
sftp.host.password = 321pepe321  
sftp.host.remote.directory.download = /appuser/mc-processor/RECIBE  
sftp.local.directory.download = encrypted  
sftp.host.remote.directory.download.filter = *.*  

logging.level.ar.com.sondeos.batch.integration.processor.integration=DEBUG  
logging.level.ar.com.sondeos.batch.integration.processor.batch=DEBUG   

logging.file=logs/mc-processor.log  

#for detailed debug
debug=false  



## JAVA GPG BIOGRAFIA

Bouncy Castle  
http://bouncycastle.org/

Bouncy Castle - GitHub  
https://github.com/bcgit/bc-java

Adapter - Bouncy GPG - GitHub  
https://github.com/neuhalje/bouncy-gpg  

Useful links  
https://stackoverflow.com/questions/19173181/bouncycastle-pgp-decrypt-and-verify  



##  SPRING INTEGRATION Y SPRING BATCH BIOGRAFIA

Spring Integration  
https://spring.io/projects/spring-integration

Spring Batch  
https://spring.io/projects/spring-batch


