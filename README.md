[![Build Status](https://travis-ci.org/HTW-Projekt-2014-Commercetools/api.svg?branch=master)](https://travis-ci.org/HTW-Projekt-2014-Commercetools/api)

[![Dependency Status](https://www.versioneye.com/user/projects/54bfae036c0035c592000069/badge.svg?style=flat)](https://www.versioneye.com/user/projects/54bfae036c0035c592000069)

[![codecov.io](https://codecov.io/github/HTW-Projekt-2014-Commercetools/api/coverage.svg?branch=master)](https://codecov.io/github/HTW-Projekt-2014-Commercetools/api?branch=master)

![codecov.io](https://codecov.io/github/HTW-Projekt-2014-Commercetools/api/branch.svg?branch=master)

Installation
----------

0. Install Java JDK 1.8 and make sure to set the correct PATH settings (JAVA_HOME). You can find it here: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html .
1. Please login into your SPHERE.IO backend account (http://admin.sphere.io).
2. Navigate to "Developers -> API Clients". 
3. Run **git clone git@github.com:HTW-Projekt-2014-Commercetools/api.git** .
4. Copy the file **api/conf/custom-application-dummy.conf** and to **api/conf/custom-application.conf**.
5. Set the following configuration values:
- **sphere.project**: Copy and paste the **project name** as marked in the SPHERE.IO Backend in red.
- **sphere.clientId**: Copy and paste the field **client_id** as marked in the backend.
- **sphere.clientSecret**: Copy and paste the **field client_secret** as listed in the backend.
6. Now refer to the How to Run section. 


How to run (Windows)
----------

If possible use the GIT Bash. It appeared to work better than using the Windows Bash.
Install sbt or activator on your local machine and run the following.

```bash
cd api
activator run
```

How to run (Unix)
----------

```bash
cd api
./sbt run
```

API-Documentation
----------

- [Stubs](#stubs)
- [Demands](#demands)
	- [Query single Demand](#query-single-demand)
		- [Ressource:](#ressource)
		- [URL Parameters:](#url-parameters)
		- [Response](#response)
	- [Create Demand](#create-demand)
		- [Ressource:](#ressource-1)
		- [Body:](#body)
		- [Response:](#response-1)
		- [Example:](#example)
	- [Update Demand](#update-demand)
		- [Ressource:](#ressource-2)
		- [URL Parameters:](#url-parameters-1)
		- [Body:](#body-1)
		- [Response:](#response-2)
		- [Example:](#example-1)
	- [Delete Demand](#delete-demand)
		- [Ressource:](#ressource-3)
		- [URL Parameters:](#url-parameters-2)
		- [Response:](#response-3)
		- [Example:](#example-2)
- [Offers](#offers)
	- [Query single Offer](#query-single-offer)
		- [Ressource:](#ressource-4)
		- [URL Parameters:](#url-parameters-3)
		- [Response](#response-4)
	- [Create Offer](#create-offer)
		- [Ressource:](#ressource-5)
		- [Body:](#body)
		- [Response:](#response-5)
		- [Example:](#example-3)
	- [Update Offer](#update-offer)
		- [Ressource:](#ressource-6)
		- [URL Parameters:](#url-parameters-4)
		- [Body:](#body-1)
		- [Response:](#response-6)
		- [Example:](#example-4)
	- [Delete Offer](#delete-offer)
		- [Ressource:](#ressource-7)
		- [URL Parameters:](#url-parameters-6)
		- [Response:](#response-7)
		- [Example:](#example-5)
	- [Get List of all Demands](#get-list-of-all-demands)
	    - [Ressource:](#ressource-8)
        - [Response:](#response-8)
        - [Example:](#example-6)



# Stubs
For the latter there are also stub implementations. You can access them by prepending `stub/` to the resource identifier.

Demands: `http://dry-depths-2035.herokuapp.com/stub/demands`

Offers: `http://dry-depths-2035.herokuapp.com/stub/offers`


# Demands

## Query single Demand
### Ressource:
GET `http://dry-depths-2035.herokuapp.com/demands/{id}`

### URL Parameters:

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | alphanumeric |

###Response
200 Ok
    
    {
        "demand":{
            "id":"9dfa3c90-85c8-46ce-b50c-3ecde596bc90",
            "version": 1
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
The request body must contain a valid DemandDraft json object

    {
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
201 Created

    {
        "demand": {
            "id": "9dfa3c90-85c8-46ce-b50c-3ecde596bc90",
            "version": 1
            "userId": "1",
            "tags": "socken bekleidung wolle",
            "location": {
                "lat":13.534212,
                "lon":52.468562
            },
            "distance": 30,
            "price": {
                "min":25.0,
                "max":77.0
            }
        }
    }
    
400 Bad Request - Empty Body

400 Bad Request - Invalid Json

### Example:

    curl -XPOST -H "Content-Type: application/json" -d '{"userId":"1","tags":"socken bekleidung wolle","location":{"lat":13.534212,"lon":52.468562},"distance":30,"price":{"min":25.0,"max":77.0}}' http://dry-depths-2035.herokuapp.com/demands -v

## Update Demand
### Ressource:
PUT `http://dry-depths-2035.herokuapp.com/demands/{id}/{version}`

### URL Parameters:

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | alphanumeric |
| version | Mandatory | numeric |


### Body:
The request body must contain a valid DemandDraft json object

    {
        "userId":"1",
        "tags":"socken bekleidung wolle rot",
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

    {
        "demand": {
            "id": "9dfa3c90-85c8-46ce-b50c-3ecde596bc90",
            "version": 2
            "userId": "1",
            "tags": "socken bekleidung wolle rot",
            "location": {
                "lat":13.534212,
                "lon":52.468562
            },
            "distance": 30,
            "price": {
                "min":25.0,
                "max":77.0
            }
        }
    }	
	
400 Bad Request - Missing body

400 Bad Request - Cannot parse json

404 Not Found - Entity was not found

### Example:

    curl -XPUT -H "Content-Type: application/json" -d '{"userId":"1","tags":"socken bekleidung wolle","location":{"lat":13.534212,"lon":52.468562},"distance":30,"price":{"min":25.0,"max":77.0}}' http://dry-depths-2035.herokuapp.com/demands/1/1 -v 

## Delete Demand
### Ressource:
DELETE `http://dry-depths-2035.herokuapp.com/demands/{id}/{version}`

### URL Parameters:

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | alphanumeric |
| version | Mandatory | numeric |


### Response:

200 Ok

404 Not Found - Entity was not found

### Example:

    curl -XDELETE http://dry-depths-2035.herokuapp.com/demands/1/1 -v
    
# Offers

## Query single Offer
### Ressource:
GET `http://dry-depths-2035.herokuapp.com/offers/{id}`

### URL Parameters:

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | alphanumeric |

###Response
200 Ok
    
    {
        "offer":{
            "id":"9dfa3c90-85c8-46ce-b50c-3ecde596bc90",
            "version": 1
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

## Create Offer
### Ressource:
POST `http://dry-depths-2035.herokuapp.com/offers`

### Body:
The request body must contain a valid OfferDraft json object

    {
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
201 Created

    {
        "offer": {
            "id": "9dfa3c90-85c8-46ce-b50c-3ecde596bc90",
            "version": 1
            "userId": "1",
            "tags": "socken bekleidung wolle",
            "location": {
                "lat":13.534212,
                "lon":52.468562
            },
            "distance": 30,
            "price": {
                "min":25.0,
                "max":77.0
            }
        }
    }
    
400 Bad Request - Empty Body

400 Bad Request - Invalid Json

### Example:

    curl -XPOST -H "Content-Type: application/json" -d '{"userId":"1","tags":"socken bekleidung wolle","location":{"lat":13.534212,"lon":52.468562},"distance":30,"price":{"min":25.0,"max":77.0}}' http://dry-depths-2035.herokuapp.com/offers -v

## Update Offer
### Ressource:
PUT `http://dry-depths-2035.herokuapp.com/offers/{id}/{version}`

### URL Parameters:

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | alphanumeric |
| version | Mandatory | numeric |


### Body:
The request body must contain a valid OfferDraft json object

    {
        "userId":"1",
        "tags":"socken bekleidung wolle rot",
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

    {
        "offer": {
            "id": "9dfa3c90-85c8-46ce-b50c-3ecde596bc90",
            "version": 2
            "userId": "1",
            "tags": "socken bekleidung wolle rot",
            "location": {
                "lat":13.534212,
                "lon":52.468562
            },
            "distance": 30,
            "price": {
                "min":25.0,
                "max":77.0
            }
        }
    }	
	
400 Bad Request - Missing body

400 Bad Request - Cannot parse json

404 Not Found - Entity was not found

### Example:

    curl -XPUT -H "Content-Type: application/json" -d '{"userId":"1","tags":"socken bekleidung wolle","location":{"lat":13.534212,"lon":52.468562},"distance":30,"price":{"min":25.0,"max":77.0}}' http://dry-depths-2035.herokuapp.com/offers/1/1 -v 

## Delete Demand
### Ressource:
DELETE `http://dry-depths-2035.herokuapp.com/offers/{id}/{version}`

### URL Parameters:

| Name | Mandatory | Value Type |
| ---- | --------- | ---------- |
| id | Mandatory | alphanumeric |
| version | Mandatory | numeric |


### Response:

200 Ok

404 Not Found - Entity was not found

### Example:

    curl -XDELETE http://dry-depths-2035.herokuapp.com/offers/1/1 -v

## Get List of all Demands
### Ressource:
GET `http://dry-depths-2035.herokuapp.com/matching/demands`


### Response:

200 Ok

### Example:

    curl -XGET http://dry-depths-2035.herokuapp.com/matching/demands -v
    
