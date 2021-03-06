# City Self-Hosting Guide
Here's all you need to know to host your own City.

## Why self-host a City
The whole idea of Beacon as a product was to make something similar to Discord or Slack but provide the user with the option to keep their data private. While the existence of the Central Identity Server (CIS) is still required, the messages that you send on any Towers you create on your City will **_never_** leave the machine you host your City on.

## Requirements
- Ownership of a domain name
  - A valid SSL certificate for a subdomain of that domain (this is where your City will be reachable from)
- A [MongoDB server](https://www.mongodb.com/try/download/community)
- Java JDK 17
- The latest LTS release of [Node.js](https://nodejs.org/en/download/) (including access to the NPM command)

## Installation
Installing the City is simple. There's really no installation process at all. Simply clone this repository. That's it!

## Configuration
Getting everything configured just right isn't too bad. Here's what you need to do before you start the City.

### Web Configuration
Configure some kind of web server to serve the content from the City. Currently, there is no support for installing an SSL certificate on the City. You _must_ use [an HTTPS to HTTP](https://www.digitalocean.com/community/tutorials/how-to-use-apache-http-server-as-reverse-proxy-using-mod_proxy-extension) [reverse proxy](https://httpd.apache.org/docs/2.4/howto/reverse_proxy.html). The service itself does not support installing SSL certificates at the moment.

You'll most likely want to use a subdomain to make your City not occupy your entire domain. If you do this, be sure your SSL certificate covers that subdomain or supports wildcard subdomains.

### Hosts Configuration
To [the City's main config file](Downloads/beacon-backend/city/src/main/resources/application.yml), you will need to add the following:
```yaml
city:
  cis-url: https://beacon-cis.herokuapp.com
  hostname: my.domain.com
```
The `cis-url` should not be changed. It tells the City where the CIS is that it will use to validate identities. The `hostname` **_MUST_** be changed such that "my.domain.com" is a valid domain that you own which points directly to the machine you intend to host this on. Do not include "https://" on this line. Only use a fully qualified domain name (FQDN).

### Database Configuration
If your MongoDB database is reachable at `localhost:27017`, you can skip this section.

If not, you'll need to add a `spring.data.mongodb.uri` line to [application.yml](Downloads/beacon-backend/city/src/main/resources/application.yml) similar to the one seen in [the production config](Downloads/beacon-backend/city/src/main/resources/application-heroku-prod.yml), substituting the URL, username, and password for those of your own database. Note that your URI should start with `mongodb://` and in most cases won't include the "+srv" part.

### Storage Configuration
When users upload files with the messages they send, they need to be stored somewhere. By default, a directory named "upload-dir" will be created alongside wherever the City was started from. To change this or to pick a different storage provider, please see the [storage providers documentation](storage-providers.md)

### Port Configuration
By default, this application runs on port 8080. To override this, set an environment variable named `PORT` to the desired port before starting the service.

## Running
Running the City is as simple as running `./gradlew clean build :city:bootRun`

This command will run in the foreground until you terminate the process with CTRL + C or by sending the SIGTERM or SIGKILL system commands. If you'd like this process to run in the background, you can use a tool like [screen](https://www.gnu.org/software/screen/manual/screen.html) ([here's](https://www.baeldung.com/linux/screen-command) a helpful getting started guide).

### Creating the first tower
At the moment, there isn't a way to create a new tower on a designated City via the web app (tracked by [frontend#21](https://github.com/IncPlusPlus/beacon-frontend/issues/21)). You'll have to use a tool like [Postman](https://www.postman.com/) to send a `POST` request to the `/towers` endpoint until this feature is implemented in the frontend.

See [the Swagger docs](https://beacon-city.herokuapp.com/swagger-ui/index.html#/Towers/createTower) for the parameters that endpoint accepts.

## Updating
To update the City, you'll need to stop the service. There currently is no way to gracefully shut the City down (see [#42](https://github.com/IncPlusPlus/beacon-backend/issues/42) for progress on this), so you'll need to stop the process yourself.

Navigate to the root of the cloned repository and run `git pull`. You can now run the service again as you had done previously.

To check that you are now up-to-date, you can visit the URL of your City directly in your browser. You should notice the words "Beacon City API" at the top of the page. To the right of it is a version number. If this changed, you should be all set.