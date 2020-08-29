const urlParams = new URLSearchParams(window.location.search);
const cartId = urlParams.get('cartId');

// Creates the checkout page
var checkoutBtn = document.createElement('a');
var btnText = document.createTextNode("Checkout");
checkoutBtn.appendChild(btnText);
checkoutBtn.title = "Checkout"
checkoutBtn.href = "confirmation.html?cartId=" + cartId;
document.getElementById("checkout-options").appendChild(checkoutBtn);