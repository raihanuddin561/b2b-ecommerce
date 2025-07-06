package com.dealkartbd.backend_app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Slf4j
public class EmailValidationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public boolean isEmailValid(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        // First check format
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            log.warn("Email format invalid: {}", email);
            return false;
        }

        String domain = email.substring(email.indexOf("@") + 1);

        try {
            // Check if domain has MX record
            return hasMXRecord(domain) && isSmtpServerReachable(domain);
        } catch (Exception e) {
            log.error("Error validating email domain {}: {}", domain, e.getMessage());
            // If validation fails due to network issues, we'll return false for strict validation
            return false;
        }
    }

    private boolean hasMXRecord(String domain) {
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            DirContext ctx = new InitialDirContext(env);

            Attributes attrs = ctx.getAttributes(domain, new String[]{"MX"});
            boolean hasMX = attrs.get("MX") != null;
            ctx.close();

            log.debug("Domain {} has MX record: {}", domain, hasMX);
            return hasMX;
        } catch (NamingException e) {
            log.warn("Could not check MX record for domain {}: {}", domain, e.getMessage());
            return false;
        }
    }
    private List<String> getMXHosts(String domain) throws Exception {
        Hashtable<String, String> env = new Hashtable<>();
        env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
        DirContext ctx = new InitialDirContext(env);
        Attributes attrs = ctx.getAttributes(domain, new String[]{"MX"});
        Attribute attr = attrs.get("MX");
        List<String> mxHosts = new ArrayList<>();
        if (attr != null) {
            for (int i = 0; i < attr.size(); i++) {
                String[] parts = attr.get(i).toString().split(" ");
                if (parts.length == 2) {
                    mxHosts.add(parts[1].endsWith(".") ? parts[1].substring(0, parts[1].length() - 1) : parts[1]);
                }
            }
        }
        ctx.close();
        return mxHosts;
    }
    private boolean isSmtpServerReachable(String domain) {
        try {
            List<String> mxHosts = getMXHosts(domain);
            int[] smtpPorts = {25, 587, 465};
            for (String host : mxHosts) {
                for (int port : smtpPorts) {
                    try (Socket socket = new Socket()) {
                        socket.connect(new java.net.InetSocketAddress(host, port), 5000);
                        log.debug("SMTP server {}:{} is reachable", host, port);
                        return true;
                    } catch (IOException e) {
                        log.debug("SMTP server {}:{} is not reachable: {}", host, port, e.getMessage());
                    }
                }
            }
            return false;
        } catch (Exception e) {
            log.warn("Error checking SMTP server reachability for {}: {}", domain, e.getMessage());
            return false;
        }
    }

    public String validateEmailWithDetails(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email address cannot be null or empty";
        }

        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return "Invalid email format";
        }

        String domain = email.substring(email.indexOf("@") + 1);

        try {
            if (!hasMXRecord(domain)) {
                return "Email domain does not have valid mail server configuration";
            }

            if (!isSmtpServerReachable(domain)) {
                return "Email server is not reachable";
            }

            return null; // Email is valid
        } catch (Exception e) {
            log.error("Error during detailed email validation for {}: {}", email, e.getMessage());
            return "Unable to verify email server availability";
        }
    }
}
