var textContainer = document.createElement("p");
var nativeText = document.createTextNode("Android Text");
textContainer.appendChild(nativeText);

var inputContainer = document.createElement("p");
var input = document.createElement("INPUT");
input.setAttribute("type", "text");
inputContainer.appendChild(input);

var buttonContainer = document.createElement("p");
var button = document.createElement("button");
button.innerHTML = "Send to Android";
button.style.width = "150px";
button.style.height = "30px";
button.addEventListener ("click", function() {
  js_interface.callEvent("showToast", {"key1":input.value, "key2":input.value});
});
buttonContainer.appendChild(button);

document.body.appendChild(textContainer);
document.body.appendChild(inputContainer);
document.body.appendChild(buttonContainer);

function sendEvent(eventName, params) {
        js_interface.callEvent({"eventName":eventName, "params":params});
    };

var colorIndex = 0;
function changeColorFromAndroid(hexColor){
    nativeText.nodeValue = hexColor;

    let colors = ["#FF0000", "#00FF00", "#0000FF"];
    colorIndex = (colorIndex + 1) % colors.length;
    let color = colors[colorIndex];

    button.style.color=hexColor;
    button.style.backgroundColor=color;
}
