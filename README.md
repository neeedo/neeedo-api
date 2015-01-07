[![Build Status](https://travis-ci.org/HTW-Projekt-2014-Commercetools/api.svg?branch=master)](https://travis-ci.org/HTW-Projekt-2014-Commercetools/api)

[![codecov.io](https://codecov.io/github/HTW-Projekt-2014-Commercetools/api/coverage.svg?branch=master)](https://codecov.io/github/HTW-Projekt-2014-Commercetools/api?branch=master)

![codecov.io](https://codecov.io/github/HTW-Projekt-2014-Commercetools/api/branch.svg?branch=master)

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

    curl -XPOST -H "Content-Type: application/json" -d '{"id":"1","userId":"1","tags":"socken bekleidung wolle","location":{"lat":13.534212,"lon":52.468562},"distance":30,"price":{"min":25.0,"max":77.0}}' http://dry-depths-2035.herokuapp.com/demands -v
