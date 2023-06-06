# University of Sofia Java Project for Modern Java Technologies Course 

A Java project I created for the Modern Java Technologies Course based on the following page: https://github.com/fmi/java-course/blob/master/course-projects/battleships.md

Additionally, I added support for multiple enemies to be able to join a single game and all play against each other in a free-for-all.

1. The project is structured using an MVC model. There are classes which handle changes and get request to the database, there are classes which act like views in the sense that they print to the console the necessary stuff that the player should see and there are controller classes which use the database handling classes in conjunction with the view classes.

2. There is a client-side module and a server-side. There is also a common module, which contains things like cookies, that are sent between the server and the client during every request/response. The client-side and the server-side modules do not directly rely on one-another.

3. There are also a few unit tests which use mocking on the database get functions to test for the correct behaviour of the controller classes.

4. Purely out of self-interest, I played around with reflection and managed to make a very basic 'web framework', which check the classes whose names end in controller for properly annotated functions and uses reflection to gather the necessary arguments to fed to the function and calls it that way automatically.
