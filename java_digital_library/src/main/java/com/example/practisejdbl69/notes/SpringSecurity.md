## Spring Security Notes

**Spring Security : Authentication followed by authorization**

## Encoding
    Transforming of data into one form to another format. which can be reversible through decoding mechanisms.
## Hashing
    Transformation of data into another format, which is not reversible (One Way transformation). It ensures that
    same hash will be generated for same values always until the under-laying logic is modified. (SHA-256 algo)
## Encryption
    Transformation of data into non-understandable or non-consumable format. (Security)
    Can be converted into original data through decryption (say, PUB-PRI key ) 

### Basic Auth

Pros:
    - We get a password on the console when we launch the spring application
Cons:
    - Cookie injection can lead to unauthorized access or Man in the middle attack

### CoRs

- XSS Attacks - Cross site forgery
  - Ex : Where cookie from one site and injecting into other sites will be prohibited
  - If u create a session id and copy a chrome browser of one laptop and paste it in another chrome
    browser of another laptop.
  - Copy cookies from one sessions to another session is also not allowed

### Things to remember
Error Code for Authorization failure: 403