# Hand Written Notes

<embed src="https://github.com/sathwickreddyy/java-projects/tree/main/java-tls-setup/notes.pdf" type="application/pdf" width="100%" height="600px" />

[View PDF](https://github.com/sathwickreddyy/java-projects/tree/main/java-tls-setup/notes.pdf)

# What is a Certificate Authority (CA)?

A Certificate Authority (CA) is like a trusted third party that acts as a "notary" for digital certificates. It verifies the identity of entities (like servers or clients) and issues certificates that prove their authenticity. These certificates are used in SSL/TLS communication to establish trust between parties.

### Think of it like this:
- The CA is a trusted organization everyone agrees to trust.
- The CA issues certificates to entities (clients or servers) after verifying their identity.
- If both parties trust the CA, they can trust each other without directly knowing each other beforehand.

---

## Why Use a CA for 2-Way SSL?

In 2-way SSL, both the client and server need to verify each other's identities:
- The server needs to ensure that the client is legitimate.
- The client needs to ensure that the server is legitimate.

### Using a CA simplifies this process:
- Instead of manually exchanging and trusting individual certificates, both the client and server trust the CA's root certificate.
- The CA signs certificates for both the client and server. Since both parties trust the CA, they automatically trust each other's certificates.

---

## How Does It Work?

Here’s how trust is established in 2-way SSL with a CA:

### 1. The CA Issues Certificates:
- The server generates a certificate signing request (CSR) and sends it to the CA.
- The client does the same.
- The CA verifies their identities and signs their certificates using its private key.

### 2. Trusting the CA:
- Both the client and server install the CA's root certificate in their respective truststores.
- This root certificate acts as proof that any certificate signed by the CA can be trusted.

### 3. SSL Handshake:
- When the client connects to the server, they exchange certificates.
- Each party verifies that the other's certificate was signed by the trusted CA.
- If verification succeeds, they establish an encrypted connection.

---

## Step-by-Step Process

### 1. Set Up Your Own Certificate Authority
You can create your own private CA if you don’t want to use a public one like Let’s Encrypt or DigiCert.

Generate a root certificate for your CA:
```
openssl genrsa -out rootCA.key 2048
openssl req -x509 -new -nodes -key rootCA.key -sha256 -days 3650 -out rootCA.pem \
    -subj "/C=US/ST=State/L=City/O=YourOrg/OU=IT/CN=RootCA"
```

This `rootCA.pem` file will be used to sign client and server certificates.

---

### 2. Generate Server and Client Certificates
For each entity (server or client):

#### Create a private key:
```
openssl genrsa -out server.key 2048
```


#### Create a CSR (Certificate Signing Request):
```
openssl req -new -key server.key -out server.csr \
    -subj "/C=US/ST=State/L=City/O=YourOrg/OU=IT/CN=server"

```

#### Sign the CSR with your CA:

```
openssl x509 -req -in server.csr -CA rootCA.pem -CAkey rootCA.key \
    -CAcreateserial -out server.crt -days 3650 -sha256

```

Repeat these steps for the client, replacing `server` with `client`.

---

### 3. Distribute Certificates
- Give each party its own private key (`server.key` or `client.key`) and signed certificate (`server.crt` or `client.crt`).
- Distribute the CA's root certificate (`rootCA.pem`) to both parties so they can verify each other's certificates.

---

### 4. Configure Truststores
Add the CA's root certificate (`rootCA.pem`) to both the server's and client's truststores.

For example, in Java:
```
keytool -importcert -alias root-ca \
    -file rootCA.pem \
    -keystore truststore.jks \
    -storepass password
```


---

### 5. Enable Mutual Authentication in Spring Boot

#### Server (`application.yml`):
```
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:server-keystore.jks
    key-store-password: password
    trust-store: classpath:server-truststore.jks
    trust-store-password: password
    client-auth: need # Enforces mutual authentication
```

#### Client (`application.yml`):
```
spring:
  ssl:
    key-store: classpath:client-keystore.jks
    key-store-password: password
    trust-store: classpath:client-truststore.jks
    trust-store-password: password
```


---

## Advantages of Using a CA

1. **Simplifies Trust Management**:
   Instead of manually trusting individual certificates, you only need to trust one root certificate (the CA).

2. **Scalability**:
   When new clients or servers are added, they just need certificates signed by the same CA—no need to update everyone else’s truststores.

3. **Revocation**:
   If a certificate is compromised, you can revoke it through a Certificate Revocation List (CRL) or Online Certificate Status Protocol (OCSP).

4. **Widely Used Practice**:
   This is how most secure systems work today, including websites using HTTPS.

---

## Real-Life Analogy

Imagine you're organizing an event where attendees need ID badges:
1. You hire an official badge provider (the CA) who verifies attendees' identities and issues badges.
2. Security guards at the event only need to recognize badges issued by this provider—they don’t need to know every attendee personally.
3. If someone loses their badge, you tell security not to accept it anymore (revocation).

Similarly, in SSL/TLS communication, the CA acts as this trusted badge provider!

By using a Certificate Authority for managing trust in your system, you make it easier to scale securely while reducing manual effort in managing individual certificates between clients and servers.

