[![Build Status](https://travis-ci.org/HTW-Projekt-2014-Commercetools/api.svg?branch=master)](https://travis-ci.org/HTW-Projekt-2014-Commercetools/api)

[![Dependency Status](https://www.versioneye.com/user/projects/54ad4141daa46ef29900001d/badge.svg?style=flat)](https://www.versioneye.com/user/projects/54ad4141daa46ef29900001d)

[![codecov.io](https://codecov.io/github/HTW-Projekt-2014-Commercetools/api/coverage.svg?branch=master)](https://codecov.io/github/HTW-Projekt-2014-Commercetools/api?branch=master)

![codecov.io](https://codecov.io/github/HTW-Projekt-2014-Commercetools/api/branch.svg?branch=master)

- [Demands](#demands)
	- [Query all Demands](#query-all-demands)
		- [Ressource:](#ressource)
		- [Response:](#response)
	- [Query single Demand](#query-single-demand)
		- [Ressource:](#ressource-1)
		- [URL Parameters:](#url-parameters)
		- [Response](#response-1)
	- [Create Demand](#create-demand)
		- [Ressource:](#ressource-2)
		- [Body:](#body)
		- [Response:](#response-2)
		- [Example:](#example)
	- [Update Demand](#update-demand)
		- [Ressource:](#ressource-3)
		- [URL Parameters:](#url-parameters-1)
		- [Body:](#body-1)
		- [Response:](#response-3)
		- [Example:](#example-1)
	- [Delete Demand](#delete-demand)
		- [Ressource:](#ressource-4)
		- [URL Parameters:](#url-parameters-2)
		- [Response:](#response-4)
		- [Example:](#example-2)

# Demands

## Query all Demands
### Ressource:
GET `http://dry-depths-2035.herokuapp.com/demands`

### Response:
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

## Query single Demand
### Ressource:
GET `http://dry-depths-2035.herokuapp.com/demands/{id}`

### URL Parameters:

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

## Create Demand
### Ressource:
POST `http://dry-depths-2035.herokuapp.com/demands`

### Body:
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

### Response:
200 Ok

400 Bad Request - Empty Body

400 Bad Request - Invalid Json

### Example:
*Note: If you hand in another ID than 1, we will return 404 NOT FOUND for simulation cases.*

    curl -XPOST -H "Content-Type: application/json" -d '{"id":"1","userId":"1","tags":"socken bekleidung wolle","location":{"lat":13.534212,"lon":52.468562},"distance":30,"price":{"min":25.0,"max":77.0}}' http://dry-depths-2035.herokuapp.com/demands -v

## Update Demand
### Ressource:
PUT `http://dry-depths-2035.herokuapp.com/demands/{id}`

### URL Parameters:

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | numeric |


### Body:
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

### Response:
200 Ok

400 Bad Request - Missing body

400 Bad Request - Cannot parse json

404 Not Found - Entity was not found

### Example:

*Note: If you hand in another ID than 1, we will return 404 NOT FOUND for simulation cases.*

    curl -XPUT -H "Content-Type: application/json" -d '{"id":"1","userId":"1","tags":"socken bekleidung wolle","location":{"lat":13.534212,"lon":52.468562},"distance":30,"price":{"min":25.0,"max":77.0}}' http://dry-depths-2035.herokuapp.com/demands/1 -v 

## Delete Demand
### Ressource:
DELETE `http://dry-depths-2035.herokuapp.com/demands/{id}`

### URL Parameters:

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | numeric |


### Response:

200 Ok

404 Not Found - Entity was not found

### Example:

*Note: If you hand in another ID than 1, we will return 404 NOT FOUND for simulation cases.*

    curl -XDELETE http://dry-depths-2035.herokuapp.com/demands/1 -v
