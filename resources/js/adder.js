var adder = function () {
  var el = $(this);
  var templateId = ".template." + el.data("template");
  var template = $(templateId).clone().removeClass("template");
  var renderTemplate = function (idx) {
    var t = template.clone();

    $("[name]", t).each(function (i, el) {
      var el = $(this);
      alert(idx);
      el.attr("name", el.attr("name").replace("[0]", "[" + idx + "]"));
    });

    return t;
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
