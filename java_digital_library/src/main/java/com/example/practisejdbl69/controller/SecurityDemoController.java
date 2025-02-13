package com.example.practisejdbl69.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityDemoController {

    @GetMapping("greet/{username}")
    public String greet(@PathVariable String username){
        return "Hello "+username;
    }

    @GetMapping("admin/greet/{username}")
    public String greetAdmin(@PathVariable String username){
        return "Hello Admin "+username;
    }

    // Helper method to get CSRF Token / Dummy method
    @GetMapping("/csrf")
    public String homePage(HttpServletRequest request){
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        return csrfToken.getToken();
    }

    @GetMapping("/readoauth")
    public String readOAuth(Authentication authentication) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(authentication);
    }
    /**
     * Sample output: we cannot debug at authentication step in AppAuthenticationProvider.java to see
     * what user details. Hit above URL
     * {
     *     "authorities": [
     *         {
     *         "authority": "OAUTH2_USER",
     *         "attributes": {
     *             "login": "sathwick18",
     *             "id": 66886237,
     *             "node_id": "MDQ6VXNlcjY2ODg2MjM3",
     *             "avatar_url": "https://avatars.githubusercontent.com/u/66886237?v=4",
     *             "gravatar_id": "",
     *             "url": "https://api.github.com/users/sathwick18",
     *             "html_url": "https://github.com/sathwick18",
     *             "followers_url": "https://api.github.com/users/sathwick18/followers",
     *             "following_url": "https://api.github.com/users/sathwick18/following{/other_user}",
     *             "gists_url": "https://api.github.com/users/sathwick18/gists{/gist_id}",
     *             "starred_url": "https://api.github.com/users/sathwick18/starred{/owner}{/repo}",
     *             "subscriptions_url": "https://api.github.com/users/sathwick18/subscriptions",
     *             "organizations_url": "https://api.github.com/users/sathwick18/orgs",
     *             "repos_url": "https://api.github.com/users/sathwick18/repos",
     *             "events_url": "https://api.github.com/users/sathwick18/events{/privacy}",
     *             "received_events_url": "https://api.github.com/users/sathwick18/received_events",
     *             "type": "User",
     *             "site_admin": false,
     *             "name": "Sathwick reddy ",
     *             "company": "Amazon",
     *             "blog": "",
     *             "location": "Bengaluru",
     *             "email": "sathwickreddyy@outlook.com",
     *             "hireable": true,
     *             "bio": null,
     *             "twitter_username": null,
     *             "public_repos": 5,
     *             "public_gists": 0,
     *             "followers": 0,
     *             "following": 0,
     *             "created_at": "2020-06-13T20:16:35Z",
     *             "updated_at": "2024-05-26T09:47:14Z",
     *             "private_gists": 0,
     *             "total_private_repos": 3,
     *             "owned_private_repos": 3,
     *             "disk_usage": 20241,
     *             "collaborators": 0,
     *             "two_factor_authentication": false,
     *             "plan": {
     *                 "name": "free",
     *                 "space": 976562499,
     *                 "collaborators": 0,
     *                 "private_repos": 10000
     *             }
     *         }
     *     },
     *     ]
     * }
     */

}
