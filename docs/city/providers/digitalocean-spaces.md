# DigitalOcean Spaces
For DigitalOcean Spaces, you'll need to add `do.spaces` properties items to the yaml.

Here's an example of what we use. You'll need to fill each of these properties with the values relevant to the Space that you create. Keep in mind that you need to create a DigitalOcean Spaces key. A normal DigitalOcean personal access token will not work.
```yaml
do:
  spaces:
    key: ${DO_SPACES_KEY}
    secret: ${DO_SPACES_SECRET}
    endpoint: nyc3.digitaloceanspaces.com
    region: nyc3
    bucket: beaconcdn
```