# kitten
A simple GAE command line interface. 

Under the hood it uses [GQL](https://github.com/pastekit/gql) to make Datastore queries.

#### Get help
`$ java -jar kitten-1.0-SNAPSHOT-shaded.jar --help`

```
NAME
        kitten - GAE Datastore command line utility

SYNOPSIS
        kitten [(-A <appId> | --appId <appId>)] [(-h | --help)]
                [(-P <password> | --pass <password>)] [(-Q <query> | --query <query>)]
                [(-U <username> | --user <username>)] [--update <update>]

OPTIONS
        -A <appId>, --appId <appId>
            GAE Application ID

        -h, --help
            Display help information

        -P <password>, --pass <password>
            GAE Password

        -Q <query>, --query <query>
            GQL Query string

        -U <username>, --user <username>
            GAE Username

        --update <update>
            Update values for result data set. E.g. --update
            property1=value1,property2=value2 etc.
```            
