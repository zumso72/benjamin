package benjamin.security

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
class Oauth2SecurityConfig : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity) {
        http.authorizeRequests {
            it.anyRequest().authenticated()
        }.oauth2ResourceServer { oauth2 ->
            oauth2.jwt()
        }
    }
}
