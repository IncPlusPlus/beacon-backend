# Beacon Backend Services
This repo contains the two backend services that power the Beacon project. The frontend can be found [here](https://github.com/IncPlusPlus/beacon-frontend). This project is for COMP4960 (Software Engineering) at Wentworth Institute of Technology.

**NOTE:** These are **_developer_** instructions. If you're a user looking to self-host your own Towers, that documentation isn't available yet. Come back later.

## Building
This project can be built and run with our without an IDE. Note that building will fail if the files aren't up to the [Google Java coding style](https://google.github.io/styleguide/javaguide.html). If the `spotless` Gradle task is failing, run the `spotlessApply` task.

### With IntelliJ
This project comes with [several](.run) shared [run configurations](https://www.jetbrains.com/help/idea/2021.3/run-debug-configuration.html). Choose them from the toolbar at the top of the IDE towards the right. Click the dropdown to choose what you want to run and then click the Run or Debug button to start.
![image](https://user-images.githubusercontent.com/6992149/156421023-ad0e5c79-c84f-4232-b368-f62e1197a8f5.png)


### With CLI
If you are building this without an IDE, you can choose to run the Gradle executable and specify the desired tasks. To execute the `clean` and `build` tasks, you would run `./gradlew clean build`. Replace `./gradlew` with `.\gradlew.bat` if you're on Windows. If you wanted to build but skip running the tests to save time, you would run `./gradlew build -x test`.

## Running
To run these services locally on your machine, you should run the CIS and/or City service from the Services tool window. If you don't see this tool window, you can summon it with Alt + 8. If that doesn't work, go to `View -> Tool Windows -> Services`.
![image](https://user-images.githubusercontent.com/6992149/156421209-2a9806cf-aa2f-4ccc-bbe7-d705b442f9af.png)

### Requirements to run these services
- You must have a MongoDB server running on `localhost:27017` for either of the services to work. They may start successfully but any request that will require them to consult the database will throw an exception.
  - You can obtain MongoDB Community Server [here](https://www.mongodb.com/try/download/community)
- At the moment (and for the foreseeable future), the City service will fail to start if the CIS service isn't already running.

## Deployments
There are several deployment strategies applicable to the backend services. Apart from any local instances you might spin up, the services are hosted by [Heroku](https://herokuapp.com/).

### Local Deployments
Local deployments make no online changes. See the `Running` section above for how to run the services yourself.

### Pull Request Apps
Upon making a pull request (draft or otherwise), a Heroku [Review App](https://devcenter.heroku.com/articles/github-integration-review-apps) will be created. On your PR, you'll see two buttons that say `View deployment`. These will open the two review apps (one for the CIS, one for the City) that get created with every new PR. The review apps are helpful because they allow pull request reviewers to have a visual way of seeing what parts of the API have changed (although reviewing the OpenAPI specification file is also good practice).

Review app URLs look like `beacon-cis-pr-15.herokuapp.com` and `beacon-city-pr-15.herokuapp.com`, for example, if they were built from PR #15.

### The QA Environment
Our "QA" environment is just the `staging` Heroku apps in our backend pipelines. The `staging` app URLs are the following.
- https://beacon-cis-main-staging.herokuapp.com/
- https://beacon-city-main-staging.herokuapp.com/

The staging apps are representative of the current state of the `main` branch of this repository. They are updated whenever there are any changes on that branch (like if a PR is merged).

### Production
Our production environments are the following URLs.
- https://beacon-cis.herokuapp.com/
- https://beacon-city.herokuapp.com/

The production apps are whatever we choose to promote from `main` to the "production" environment. This happens with each point in time that we deem the API to be complete and stable.

## OpenAPI Specifications / API Documentation
The API for the backend services follow a documentation-first approach. The OpenAPI specifications for each service are used at build time to generate Java interfaces that are to be implemented in the codebase. This keeps the code behavior consistent with what the API documentation displays. The [CIS](common/src/main/openapi/beacon-central-identity-server.openapi.yml) spec may be found here and the City spec may be found [here](common/src/main/openapi/beacon-city.openapi.yml).

If you want to view the _rendered_ version of the specification, you may do so by visiting the URL applicable to your current deployment scenario. If you're trying to view the changes you made locally, you'd visit the URL your service is running on (http://localhost:9876 for the CIS, http://localhost:8080 for the City). Note that if you make any changes to the spec, you'll need to restart the service so the Gradle `staticallyServeOpenAPISpec` task will be run again. You're in the right place if you see a web page with a layout like the image below.
![image](https://user-images.githubusercontent.com/6992149/156421397-d951c851-e116-46c8-ac19-3c43cd3f4d21.png)

## Manually Interacting with the Services
To manually send HTTP requests to the services, you may either use the Swagger documentation page or install [Postman](https://www.postman.com/).

### With Swagger
To use Swagger to send API requests, visit the Swagger page applicable to the scenario you're in. Using Swagger to hit the API deployed on pull request apps is not possible at this time. Swagger can only send requests to local, QA, and production environments.

Before attempting to send a request, you must choose the URL you want to hit. This can be done by clicking the dropdown menu on the left side near the top of the web page.
![image](https://user-images.githubusercontent.com/6992149/156422516-60e29c56-a5bf-4c71-b15c-ad46ba76379e.png)

### With Postman
Make a new Postman request, point it to the URL you're trying to test, and off you go. Use the authorization tab as necessary. Endpoints that show a lock next to them on the Swagger page require a valid user account to be used in the authorization tab.

## External Services
Both the CIS and City services depend on a connection to a MongoDB server. Local services require a locally running MongoDB server (as noted in the `Running` section above). Each of the Heroku deployments are preconfigured to use an exising MongoDB Atlas cluster.

- PR Review Apps use `beacon-east.lw78i.mongodb.net`. They use different DB names to avoid conflicts.
  - The "main-staging" apps are configured to use the databases named `beacon-cis-main-staging` and `beacon-city-main-staging`
  - The review apps are configured to use databases named from their PR #
- Production services point at `prodcluster0.bdjwu.mongodb.net`

## Services Summary
The backend is made of two services, the Central Identity Server (CIS) and the City. Detailed explanation of services to come at a later time.

### Central Identity Server (CIS)
[//]: # (TODO)

### City
[//]: # (TODO)
