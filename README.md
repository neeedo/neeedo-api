[![Build Status](https://travis-ci.org/HTW-Projekt-2014-Commercetools/api.svg?branch=master)](https://travis-ci.org/HTW-Projekt-2014-Commercetools/api)

[![codecov.io](https://codecov.io/github/HTW-Projekt-2014-Commercetools/api/coverage.svg?branch=master)](https://codecov.io/github/HTW-Projekt-2014-Commercetools/api?branch=master)

![codecov.io](https://codecov.io/github/HTW-Projekt-2014-Commercetools/api/branch.svg?branch=master)

# Demands

## Demand erstellen
###Ressource:
`http://dry-depths-2035.herokuapp.com/demands/{id}`

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

###Response:
200 Ok

400 Bad Request - Empty Body

400 Bad Request - Invalid Json

###Example:

    curl -XPOST -H "Content-Type: application/json" -d '{"id":"1","userId":"1","tags":"socken bekleidung wolle","location":{"lat":13.534212,"lon":52.468562},"distance":30,"price":{"min":25.0,"max":77.0}}' 127.0.0.1:9000/demands -v