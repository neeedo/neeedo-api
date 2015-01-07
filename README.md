[![Build Status](https://travis-ci.org/HTW-Projekt-2014-Commercetools/api.svg?branch=master)](https://travis-ci.org/HTW-Projekt-2014-Commercetools/api)

[![Dependency Status](https://www.versioneye.com/java/io.sphere.sdk.jvm:models/1.0.0-M7/badge.svg)](https://www.versioneye.com/java/io.sphere.sdk.jvm:models/1.0.0-M7)

[![codecov.io](https://codecov.io/github/HTW-Projekt-2014-Commercetools/api/coverage.svg?branch=master)](https://codecov.io/github/HTW-Projekt-2014-Commercetools/api?branch=master)

![codecov.io](https://codecov.io/github/HTW-Projekt-2014-Commercetools/api/branch.svg?branch=master)

- [Demands](#demands)
	- [Alle Demands abrufen](#alle-demands-abrufen)
		- [Ressource:](#ressource)
		- [Response:](#response)
	- [Eine Demand abrufen](#eine-demand-abrufen)
		- [Ressource:](#ressource-1)
		- [URL - Parameter:](#url---parameter)
		- [Response](#response-1)
	- [Demand erstellen](#demand-erstellen)
		- [Ressource:](#ressource-2)
		- [Body:](#body)
		- [Response:](#response-2)
		- [Example:](#example)
	- [Demand aktualisieren](#demand-aktualisieren)
		- [Ressource:](#ressource-3)
		- [URL - Parameter:](#url---parameter-1)
		- [Body:](#body-1)
		- [Response:](#response-3)
		- [Example:](#example-1)
	- [Demand loeschen](#demand-loeschen)
		- [Ressource:](#ressource-4)
		- [URL - Parameter:](#url---parameter-2)
		- [Response:](#response-4)
		- [Example:](#example-2)

# Demands

## Alle Demands abrufen
###Ressource:
GET `http://dry-depths-2035.herokuapp.com/demands`

###Response:
    {  
       "demands":[  
          {  
             "id":"1",
             "userId":"1",
             "tags":"socken bekleidung wolle",
             "location":{  
                "lat":13.534212,
                "lon":52.468562
             },
             "distance":30,
             "price":{  
                "min":25.0,
                "max":77.0
             }
          },
          {  
             "id":"2",
             "userId":"2",
             "tags":"auto lack blau",
             "location":{  
                "lat":13.534212,
                "lon":52.468562
             },
             "distance":40,
             "price":{  
                "min":150.0,
                "max":300.0
             }
          },
          {  
             "id":"3",
             "userId":"3",
             "tags":"notebook kein apple scheiss",
             "location":{  
                "lat":10.0,
                "lon":20.0
             },
             "distance":25,
             "price":{  
                "min":500.0,
                "max":1000.0
             }
          }
       ]
    }

## Eine Demand abrufen
###Ressource:
GET `http://dry-depths-2035.herokuapp.com/demands/{id}`

###URL - Parameter:

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | numeric |

###Response
200 Ok
    
    {
        "demand":{
            "id":"1",
            "userId":"1",
            "tags":"socken bekleidung wolle",
            "location":{
                "lat":13.534212,
                "lon":52.468562
            },
            "distance":30,
            "price":{
                "min":25.0,
                "max":77.0
            }
        }
    }
    
404 - Not Found

## Demand erstellen
###Ressource:
POST `http://dry-depths-2035.herokuapp.com/demands`

###Body:
The request body must contain a valid demand json object

    {
        "id":"1",
        "userId":"1",
        "tags":"socken bekleidung wolle",
        "location":{
            "lat":13.534212,
            "lon":52.468562
        },
        "distance":30,
        "price":{
            "min":25.0,
            "max":77.0
        }
    }

###Response:
200 Ok

400 Bad Request - Empty Body

400 Bad Request - Invalid Json

###Example:
*Note: If you hand in another ID than 1, we will return 404 NOT FOUND for simulation cases.*

    curl -XPOST -H "Content-Type: application/json" -d '{"id":"1","userId":"1","tags":"socken bekleidung wolle","location":{"lat":13.534212,"lon":52.468562},"distance":30,"price":{"min":25.0,"max":77.0}}' http://dry-depths-2035.herokuapp.com/demands -v

## Demand aktualisieren
###Ressource:
PUT `http://dry-depths-2035.herokuapp.com/demands/{id}`

###URL - Parameter:

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | numeric |


###Body:
The request body must contain a valid demand json object

    {
        "id":"1",
        "userId":"1",
        "tags":"socken bekleidung wolle",
        "location":{
            "lat":13.534212,
            "lon":52.468562
        },
        "distance":30,
        "price":{
            "min":25.0,
            "max":77.0
        }
    }
	
*Note: The field ID will be removed later, but is actually required for our intern JSON mapping.*
*Also note: Later, we will change the API to only update the given fields, we don't expect the complete entity.*

###Response:
200 Ok

400 Bad Request - Missing body

400 Bad Request - Cannot parse json

404 Not Found - Entity was not found

###Example:

*Note: If you hand in another ID than 1, we will return 404 NOT FOUND for simulation cases.*

    curl -XPUT -H "Content-Type: application/json" -d '{"id":"1","userId":"1","tags":"socken bekleidung wolle","location":{"lat":13.534212,"lon":52.468562},"distance":30,"price":{"min":25.0,"max":77.0}}' http://dry-depths-2035.herokuapp.com/demands/1 -v 

## Demand loeschen
###Ressource:
DELETE `http://dry-depths-2035.herokuapp.com/demands/{id}`

###URL - Parameter:

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | numeric |


###Response:

200 Ok

404 Not Found - Entity was not found

###Example:

*Note: If you hand in another ID than 1, we will return 404 NOT FOUND for simulation cases.*

    curl -XDELETE http://dry-depths-2035.herokuapp.com/demands/1 -v
