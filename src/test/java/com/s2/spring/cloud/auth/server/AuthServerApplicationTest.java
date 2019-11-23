package com.s2.spring.cloud.auth.server;

import static org.junit.Assert.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import com.s2.spring.cloud.auth.server.user.model.User;
import com.s2.spring.cloud.auth.server.user.repository.UserRepository;



@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = AuthServerApplication.class)
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude={MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class AuthServerApplicationTest {

	@Autowired
	private WebApplicationContext wac;
	private MockMvc mockMvc;

	@MockBean
	private UserRepository userRepository;

	@Autowired
	private FilterChainProxy springSecurityFilterChain;

	private static final String CLIENT_ID = "s2-client";
	private static final String CLIENT_SECRET = "secret";

	private static final String CONTENT_TYPE = "application/json;charset=UTF-8";

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).addFilter(springSecurityFilterChain).build();
	}

	private User getMockUser() {
		User user = new User();
		user.setId("123");
		user.setUserId("123");
		user.setPassword("$2a$10$5f9TNSlN3trZTroNC8iS4.y//t89di.6UjoV99T3LFoud3B8Wn24C");
		List<String> roles = new ArrayList<String>();
		roles.add("RIDER");user.setRoles(roles);
		return user;
	}
	@Test
	public void testObtainAccessToken() throws Exception {
		Mockito.when(userRepository.findByUserId(Mockito.anyString())).thenReturn(getMockUser());

		final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", "password");
		params.add("client_id", CLIENT_ID);
		params.add("username", "123");
		params.add("password", "test");

		// @formatter:off

		ResultActions result = mockMvc
				.perform(post("/oauth/token").params(params).with(httpBasic(CLIENT_ID, CLIENT_SECRET))
						.accept(CONTENT_TYPE))
				.andExpect(status().isOk()).andExpect(content().contentType(CONTENT_TYPE));

		// @formatter:on

		String resultString = result.andReturn().getResponse().getContentAsString();

		JacksonJsonParser jsonParser = new JacksonJsonParser();
		assertNotNull(jsonParser.parseMap(resultString).get("access_token").toString());
	}
	
	@Test
	public void testUserNotFound() throws Exception {
		Mockito.when(userRepository.findByUserId(Mockito.anyString())).thenReturn(null);

		final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", "password");
		params.add("client_id", CLIENT_ID);
		params.add("username", "123");
		params.add("password", "test");

		// @formatter:off

		ResultActions result = mockMvc
				.perform(post("/oauth/token").params(params).with(httpBasic(CLIENT_ID, CLIENT_SECRET))
						.accept(CONTENT_TYPE))
				.andExpect(status().isBadRequest()).andExpect(content().contentType(CONTENT_TYPE));

	}
}
