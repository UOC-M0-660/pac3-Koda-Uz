# PARTE TEORICA

### Lifecycle

#### Explica el ciclo de vida de una Activity.
Son los difrentes estados que atraviesa una Activity en Android cuando esta se crea, se destrye,
se reanuda, etc.

##### ¿Por qué vinculamos las tareas de red a los componentes UI de la aplicación?
Para poder actualizar la UI en cuanto se recibe la respuesta de la red y para poder cancelar
todas las corrutinas iniciadas si se destruye la activity.

##### ¿Qué pasaría si intentamos actualizar la recyclerview con nuevos streams después de que el usuario haya cerrado la aplicación?
Se produciría un excepción, ya que la activity se encuentra detenida o destruida.
El uso de lifecycleScope tiene por objetivo evitar esta situación.

##### Describe brevemente los principales estados del ciclo de vida de una Activity.
Los estados que puede atravesar una Activity son los siguientes:

 - Created: Se activa cuando el sistema crea la actividad por primera vez

 - Started: La activity se muestra al usuario

 - Resumed: La activity vuelve a ser visible después de ser pausada

 - Paused: La activity ya no se encuentra en primer plano

 - Stopped: La activity ya no puede ser vista por el usuario

 - Destroyed: Finaliza la activity, sus componentes se destruyen

---

### Paginación 

#### Explica el uso de paginación en la API de Twitch.
El uso de la paginación nos permite obtener largas listas de datos de manera progresiva
a medida que los solicita el usuario, optimizando así el consumo de datos y el uso de la red.

##### ¿Qué ventajas ofrece la paginación a la aplicación?
Podemos obtener una larga lista de Streams a medida que los solicita el usuario,
mediante el uso de la paginación podemos obtener estos de 20 en 20.
Cuando el usuario entre en streamsActivity verá los primeros 20, si llega al final de la lista,
se solicitarán otros 20 y se añadirán a esta, así siucesivamente.

##### ¿Qué problemas puede tener la aplicación si no se utiliza paginación?
Nuestra aplicación consumiría demasiados recursos, tanto del dispositivo como de red, descargando
cantidad de datos innecesarios.
Esto podría provocar congelaciones en la app y un gasto excesivo de datos en la tarifa de móvil
del usuario.

##### Lista algunos ejemplos de aplicaciones que usan paginación.
Muchas de las aplicaciones más descargadas hoy en día la utilizan, especialmente las redes
sociales, ya que estas cargan Feeds de publicaciones muy extensos.
Algunos ejemplos son Twitter, Instagram, Youtube, Reddit, etc.
