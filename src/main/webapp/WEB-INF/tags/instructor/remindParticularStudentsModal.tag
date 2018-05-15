<%@ tag trimDirectiveWhitespaces="true" %>
<%@ tag description="instructorHome / instructorFeedbacks - Remind modal" pageEncoding="UTF-8" %>
<%@ tag import="teammates.common.util.Const" %>
<%@ attribute name="remindParticularStudentsLink" required="true" %>

<div class="modal fade" id="remindModal" tabindex="-1" role="dialog"
    aria-labelledby="remindModal" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <form method="post" name="form_remind_list" role="form"
          action="${remindParticularStudentsLink}">
        <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal"
              aria-hidden="true">&times;</button>
          <h4 class="modal-title">
            Remind Particular Students
            <small>(Select the student(s) you want to remind)</small>
          </h4>
          <div class="checkbox">
            <input id="remind_all" type="checkbox" value="">
            <label for="remind_all">
              <strong>
                Select all students
              </strong>
            </label>
          </div>
          <div class="checkbox">
            <input id="remind_not_submitted" type="checkbox" value="">
            <label for="remind_not_submitted">
              <strong>
                Select all students not submitted
              </strong>
            </label>
          </div>
        </div>
        <div class="modal-body">
          <div id="studentList" class="form-group"></div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-default"
              data-dismiss="modal">Cancel</button>
          <input type="button" class="btn btn-primary remind-particular-button" data-dismiss="modal" value="Remind">
          <input type="hidden" name="<%= Const.ParamsNames.USER_ID %>"
              value="${data.account.googleId}">
        </div>
      </form>
    </div>
  </div>
</div>
