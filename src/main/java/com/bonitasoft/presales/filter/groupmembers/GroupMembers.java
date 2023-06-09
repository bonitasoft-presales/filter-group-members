package com.bonitasoft.presales.filter.groupmembers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.filter.AbstractUserFilter;
import org.bonitasoft.engine.filter.UserFilterException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.GroupNotFoundException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserSearchDescriptor;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupMembers extends AbstractUserFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(GroupMembers.class.getName());

  public static final String GROUP_PATH_INPUT = "groupPath";
  public static final int MAX_RESULTS = 100;

  /**
   * Perform validation on the inputs defined on the actorfilter definition
   * (src/main/resources/groupMembersFilter.def) You should: - validate that mandatory inputs are
   * presents - validate that the content of the inputs is coherent with your use case (e.g:
   * validate that a date is / isn't in the past ...)
   */
  @Override
  public void validateInputParameters() throws ConnectorValidationException {
    validateStringInputParameterIsNotNulOrEmpty(GROUP_PATH_INPUT);
  }

  /**
   * @return a list of {@link User} id that are the candidates to execute the task where this filter
   *     is defined. If the result contains a unique user, the task will automatically be assigned.
   * @see AbstractUserFilter#shouldAutoAssignTaskIfSingleResult()
   */
  @Override
  public List<Long> filter(String actorName) throws UserFilterException {
    List<Long> userIds = new ArrayList<>();
    final String groupPath = getInputParameter(GROUP_PATH_INPUT).toString();
    final int startIndex = 0;
    LOGGER.info(String.format("%s input = %s", GROUP_PATH_INPUT, groupPath));
    APIAccessor apiAccessor = getAPIAccessor();
    IdentityAPI identityAPI = apiAccessor.getIdentityAPI();
    try {
      Group group = getGroup(identityAPI, groupPath);
      int currentIndex = startIndex;
      long added = addResults(currentIndex, identityAPI, group, userIds);
      while (added == MAX_RESULTS) {
        currentIndex += MAX_RESULTS;
        added = addResults(currentIndex, identityAPI, group, userIds);
      }
      LOGGER.info("found {} users in group {}",userIds.size(),group.getDisplayName());
      return userIds;
    } catch (GroupNotFoundException | SearchException e) {
      throw new UserFilterException(e);
    }
  }

  private static long addResults(
      int startIndex, IdentityAPI identityAPI, Group group, List<Long> userIds)
      throws SearchException {
    SearchOptionsBuilder builder = new SearchOptionsBuilder(startIndex, MAX_RESULTS);
    builder.filter(UserSearchDescriptor.GROUP_ID, group.getId());
    final SearchResult<User> searchResult = identityAPI.searchUsers(builder.done());
    userIds.addAll(
        searchResult.getResult().stream()
            .map(
                user -> {
                  LOGGER.debug("adding user {} - {}",user.getUserName(),user.getId());
                  return user.getId();
                })
            .collect(Collectors.toList()));
    return searchResult.getCount();
  }

  Group getGroup(IdentityAPI identityAPI, String groupPath) throws GroupNotFoundException {
    return identityAPI.getGroupByPath(groupPath);
  }
}
