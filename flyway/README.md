# SeicentoSalary - Flyway
![Produktlogo](https://flywaydb.org/assets/logo/flyway-logo-tm-sm.png "Flyway Logo")  
[Flyway](https://flywaydb.org/) is a Tool to version your database. It works with plain sql and can be used in different ways.

For using SeicentoSalary you have to setup a DB first.

## Instruction Steps
1. Get [Flyway for Command Line](https://flywaydb.org/documentation/commandline/#download-and-installation)
2. Create a new and empty DB with [SQL Managment Studio](https://docs.microsoft.com/en-us/sql/ssms/download-sql-server-management-studio-ssms?view=sql-server-2017)
3. Configure conf/flyway.conf with the DB Credentials from Step 1
4. Copy the [SQL](https://github.com/jmurighub/SeicentoSalary/tree/master/flyway/sql) Scripts to flyway (Directory flyway/sql)
5. Execute _flyway migrate_ from the command line
6. Execute _flyway info_ from the command line

Step 4:     
![flywaysql](https://github.com/jmurighub/SeicentoSalary/blob/master/docs/images/flyway-copyscripts.PNG "flyway sql")

Step 6:   
![flyway info](https://github.com/jmurighub/SeicentoSalary/blob/master/docs/images/flyway-infosalary.PNG "flyway info")

Database is ready:  
![sql management studio](https://github.com/jmurighub/SeicentoSalary/blob/master/docs/images/sqlmanagmentstudio.PNG "sql managment studio")


## Next Step
Setup Docker