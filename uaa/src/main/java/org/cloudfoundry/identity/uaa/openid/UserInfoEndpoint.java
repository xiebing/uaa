package org.cloudfoundry.identity.uaa.openid;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.cloudfoundry.identity.uaa.authentication.UaaPrincipal;
import org.cloudfoundry.identity.uaa.user.UaaUser;
import org.cloudfoundry.identity.uaa.user.UaaUserDatabase;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller that sends user info to clients wishing to authenticate.
 * 
 * @author Dave Syer
 */
@Controller
public class UserInfoEndpoint implements InitializingBean {

	private UaaUserDatabase userDatabase;

	public void setUserDatabase(UaaUserDatabase userDatabase) {
		this.userDatabase = userDatabase;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.state(userDatabase != null, "A user database must be provided");
	}

	@RequestMapping(value = "/userinfo")
	@ResponseBody
	public Map<String, String> loginInfo(Principal principal) {
		OAuth2Authentication authentication = (OAuth2Authentication) principal;
		UaaPrincipal uaaPrincipal = (UaaPrincipal) authentication.getUserAuthentication().getPrincipal();
		return getResponse(uaaPrincipal);
	}

	protected Map<String, String> getResponse(UaaPrincipal principal) {
		UaaUser user = userDatabase.retrieveUserByName(principal.getName());
		Map<String, String> response = new LinkedHashMap<String, String>() {
			public String put(String key, String value) {
				if (StringUtils.hasText(value)) {
					return super.put(key, value);
				}
				return null;
			}
		};
		response.put("user_id", user.getUsername());
		response.put("name", (user.getGivenName() != null ? user.getGivenName() : "")
				+ (user.getFamilyName() != null ? " " + user.getFamilyName() : ""));
		response.put("email", user.getEmail());
		// TODO: other attributes
		return response;
	}
}