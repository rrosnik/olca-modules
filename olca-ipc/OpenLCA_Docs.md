to see the documentation of IPC server JSON schema: https://greendelta.github.io/openLCA-ApiDoc/intro.html

# some note about the openlca engine

- default providr id is not necessary
- each exchange should have process id which in derby database schema is f_owner
- some of the materials are not used in the processes so when you add amount to them it does not make any change in result.
- new flows when they are being created in engine the ecoinvent flows should not be there but the flow property factors should be inserted.
- ipc server can be run using multithread mode which makes it more faster to calculate the result
