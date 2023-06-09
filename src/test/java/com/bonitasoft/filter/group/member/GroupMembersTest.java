package com.bonitasoft.filter.group.member;

import static com.bonitasoft.filter.group.members.GroupMembers.GROUP_PATH_INPUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bonitasoft.filter.group.members.GroupMembers;
import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupMembersTest {

  @InjectMocks private GroupMembers filter;

  @Mock(lenient = true)
  private APIAccessor apiAccessor;

  @Mock(lenient = true)
  private IdentityAPI identityAPI;

  @Mock(lenient = true)
  private Group group;

  @Mock SearchOptions option;
  @Mock SearchResult<User> results;

  @Mock User userA;

  @Mock User userB;

  @BeforeEach
  void setUp() {
    when(apiAccessor.getIdentityAPI()).thenReturn(identityAPI);
  }

  @Test
  public void should_throw_exception_if_mandatory_input_is_missing() {
    assertThrows(ConnectorValidationException.class, () -> filter.validateInputParameters());
  }

  @Test
  public void should_throw_exception_if_mandatory_input_is_null() {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put(GROUP_PATH_INPUT, null);
    filter.setInputParameters(parameters);
    assertThrows(ConnectorValidationException.class, () -> filter.validateInputParameters());
  }

  @Test
  public void should_throw_exception_if_mandatory_input_is_empty() {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put(GROUP_PATH_INPUT, "");
    filter.setInputParameters(parameters);
    assertThrows(ConnectorValidationException.class, () -> filter.validateInputParameters());
  }

  @Test
  public void should_return_a_list_of_candidates() throws Exception {
    // Given
    when(identityAPI.getGroupByPath("/acme")).thenReturn(group);
    when(identityAPI.searchUsers(any(SearchOptions.class))).thenReturn(results);
    when(results.getResult()).thenReturn(List.of(userA, userB));
    when(results.getCount()).thenReturn(2L);
    when(userA.getId()).thenReturn(456L);
    when(userB.getId()).thenReturn(789L);

    Map<String, Object> parameters = new HashMap<>();
    parameters.put(GROUP_PATH_INPUT, "/acme");
    filter.setInputParameters(parameters);

    // When
    List<Long> candidates = filter.filter("MyActor");

    // Then
    assertThat(candidates).as("Only users group can be candidates.").containsExactly(456L, 789L);
  }
}
