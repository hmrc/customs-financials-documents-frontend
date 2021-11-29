
# customs-financials-documents-frontend

| Path                                                                   | Description                                                                                       |
| ---------------------------------------------------------------------  | ------------------------------------------------------------------------------------------------- |
| GET  /postponed-vat                                                    | Retrieve postponed vat/ historic postponed vat statements                                                |                
| GET  /adjustments                                                      | Retrieve security statements for EORI                                                          |                
| GET  /import-vat                                                       | Retrieve vat certificates for EORI                                                          |                


#### Service Manager Commands

Start the required development services (make sure your service-manager-config folder is up to date)

    sm --start CUSTOMS_FINANCIALS_ALL -f

Once these services are running, use the following command to start the service `sbt "run 9398"`
    
### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").