# ee-remote-admin-access an EJB for IPv4 based authentication

This EJB delivers a `@RequiresAdmin` annotation, for rest calls that needs ip validation.
It can be placed upon the entire class or on a single method.

It's included like this:

        <dependency>
            <groupId>dk.dbc</groupId>
            <artifactId>ee-remote-admin-access</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>


## Access Control

Configuration of the bean is done with environment variables:

* ADMIN_IP a comma separated list of ip, ip-range or net of admin allowed hosts (only ipv4).

    This is functionally required, however it doesn't fail if it's present, but then all access is unauthorized.

* X_FORWARDED_FOR (optional) a comma separated list of ip, ip-range or net of trusted proxies (only ipv4)
  that can add `X-Forwarded-For` HTTP headers.

## Example

        @GET
        @Path("dangerous")
        @RequiresAdmin
        public Response somethingProtected() {
            ...
            return Response.
        }
