var adder = function () {
  var el = $(this);
  var templateId = ".template." + el.data("template");
  var template = $(templateId)
      .remove()
      .clone()
      .removeClass("template")
  var renderTemplate = function (idx) {
    var t = template.clone();

    $("[name]", t).each(function (i, el) {
      var el = $(this);
      el.attr("name", el.attr("name").replace(/\[[0-9]\]/, "[" + idx + "]"));
      if (el.attr("type") != "radio") {
        el.val("");
      } else if (el.is(":checked")) {
        el.click();
      }
    });

    return t.show();
  };

  $(this).on("click", function (event) {
    var numSaves = $("." + el.data("template")).length;

    event.preventDefault();
    el.before(renderTemplate(numSaves));

    return false;
  });
};

var adders = function () {
  $.fn.adder = adder;
  $(".adder.btn").each(adder);
};

$(document).ready(adders);
