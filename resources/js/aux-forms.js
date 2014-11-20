var submit = function () {
  return $("form#stat-block").submit();
};

var auxForms = function () {
  $("form.aux").show()
    .find("[type=submit]").on("click", submit);
};

$(document).ready(auxForms);
