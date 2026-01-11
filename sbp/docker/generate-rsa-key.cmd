REM Generate private key
openssl genpkey -algorithm RSA -out private-key.pem
REM Extract public key from private key
openssl rsa -pubout -in private-key.pem -out public-key.pem
REM Convert to PCKS
openssl pkcs8 -topk8 -inform PEM -outform PEM -in private-key.pem -out private-key.pem -nocrypt