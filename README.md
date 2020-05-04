# TextProc ![Maven CI](https://github.com/aggarcia3/TextProc/workflows/Maven%20CI/badge.svg)
TextProc is an automated text processing tool that efficiently and flexibly applies NLP to input documents in a relational database.

## Architecture and documentation
This project is structured as a multi-module Maven project for Java 11 and later (tested up to Java 14), where each Maven module corresponds to one JPMS module too.
The usage of the Java modules introduced in Java 9 allows for better coupling management, potentially increasing code quality.

This application executes a preprocessing process definition like the one at `sample_process.xml`, which is composed of steps that
are applied in sequence to input text documents in a relational database, that by default is a SQLite database named `corpus.db`. The used database can be changed in `TextProcPersistence/pom.xml` to any that Hibernate supports, and a example of database schema compatible with this application is at `TextProc.sql`. On Unix-like systems, the application can be launched with the `launch.sh` script.

For more information about the application and the steps bundled with it, please check out the [documentation website](https://aggarcia3.github.io/TextProc). The Javadoc for the project classes is available there, under the "Project Reports" section of the sidebar.
