var clicked = function (event) {
  var el = $(event.target),
      action = el.parents(".action"),
      clickedSelector = "." + el.val();

  action.find(".action-type").not(clickedSelector).hide();
  action.find(".action-type input, .action-type textarea").attr({disabled: true});

  action.find(".action-type" + clickedSelector)
    .show()
    .find("input, textarea").attr({disabled: false});
}

var actionType = function () {
  $(this).on("click", ".action-type-radio", clicked)
  $(this).find("[value=attack].action-type-radio").click();
};

var actionTypes = function () {
  $.fn.actionType = actionType;
  $("#actions-icky").each(actionType);
};

$(document).ready(actionTypes);
