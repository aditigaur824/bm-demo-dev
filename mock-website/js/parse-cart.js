const imageMap = new Map();
imageMap.set("Nike Neon and Grey Running Shoes", "images/home/neon_yellow_running_shoes.jpg");
imageMap.set("Asics Blue Running Shoes", "images/home/blue_running_shoes.jpeg");
imageMap.set("Adidas Neon Running Shoes", "images/home/neon_running_shoes.jpg");
imageMap.set("Asics Pink Running Shoes", "images/home/pink_running_shoes.jpeg");
imageMap.set("Asics Purple Running Shoes", "images/home/teal_running_shoes.jpeg");
imageMap.set("Asics White Running Shoes", "images/home/white_running_shoes.jpg");
imageMap.set("New Balance Black Running Shoes", "images/home/black_running_shoes.jpg");
imageMap.set("New Balance White Running Shoes", "images/home/white_running_shoes_nb.jpeg");
imageMap.set("Nike Neon Blue Running Shoes", "images/home/blue_neon_running_shoes_nike.jpg");
imageMap.set("Nike Navy Running Shoes", "images/home/blue_running_shoes_nike.jpg");
imageMap.set("Asics Neon Orange Running Shoes", "images/home/orange_neon_running_shoes_asics.jpeg");
imageMap.set("New Balance Neon Pink Running Shoes", "images/home/pink_neon_running_shoes_nb.jpg");
imageMap.set("Asics Pink and Purple Running Shoes", "images/home/pink_purple_running_shoes.jpeg");
const priceMap = new Map();
priceMap.set("Nike Neon and Grey Running Shoes", "80");
priceMap.set("Asics Blue Running Shoes", "55");
priceMap.set("Adidas Neon Running Shoes", "75");
priceMap.set("Asics Pink Running Shoes", "65");
priceMap.set("Asics Purple Running Shoes", "70");
priceMap.set("Asics White Running Shoes", "55");
priceMap.set("New Balance Black Running Shoes", "60");
priceMap.set("New Balance White Running Shoes", "65");
priceMap.set("Nike Neon Blue Running Shoes", "80");
priceMap.set("Nike Navy Running Shoes", "80");
priceMap.set("Asics Neon Orange Running Shoes", "70");
priceMap.set("New Balance Neon Pink Running Shoes", "65");
priceMap.set("Asics Pink and Purple Running Shoes", "60");

var urlParams = new URLSearchParams(window.location.search);
var cartId = urlParams.get('cartId');
console.log(cartId)

var botUrl = 'https://rbm-boot-camp-15.wl.r.appspot.com/cart?cartId=' + cartId; 
var getCart = async function(url) {
    let response = await fetch(url, { 
        headers: {'Content-Type': 'application/json'}
    });
    return JSON.parse(await response.text());
};

getCart(botUrl).then(cart => {
    var subTotal = 0;
    var table = document.getElementById("cart-items-table");
    var i;
    const items = cart.items;
    for (i = 0; i < cart.items.length; i++) {
        var newRow = table.insertRow(0);

        var imageCell = newRow.insertCell(0);
        var image = document.createElement('img');
        image.src = imageMap.get(cart.items[i].itemTitle);
        image.style.height = "150px";
        image.style.width = "200px";
        imageCell.appendChild(image);

        var titleCell = newRow.insertCell(1);
        titleCell.appendChild(
            document.createTextNode(
                cart.items[i].itemTitle));

        var priceCell = newRow.insertCell(2);
        priceCell.appendChild(
            document.createTextNode(
                priceMap.get(cart.items[i].itemTitle)));
        subTotal += parseInt(priceMap.get(cart.items[i].itemTitle));
        
        var quantityCell = newRow.insertCell(3);
        var quantityButton =  document.createElement("cart_quantity_button");
        quantityButton.appendChild(
            document.createElement("cart_quantity_up")
        );
        quantityButton.appendChild(
            document.createTextNode(
                cart.items[i].itemCount)
        );
        quantityButton.appendChild(
            document.createElement("cart_quantity_down")
        );
        quantityCell.appendChild(quantityButton);
        
    }
    
    //Formation of the order total
    var totalTable = document.getElementById("cart-price-total");
        
    var subtotalRow = totalTable.insertRow(0);
    var subtotalLabelCell = subtotalRow.insertCell(0);
    subtotalLabelCell.appendChild(
        document.createTextNode(
            "Cart Sub-Total:"));
    var subtotalCell  = subtotalRow.insertCell(1);
    subtotalCell.appendChild(
        document.createTextNode(
            '$' + subTotal.toString()));  
            
    var taxRow = totalTable.insertRow(1);
    var taxLabelCell = taxRow.insertCell(0);
    taxLabelCell.appendChild(
        document.createTextNode(
            "Tax:"
        )
    );
    var taxCell = taxRow.insertCell(1);
    taxCell.appendChild(
        document.createTextNode(
            "$6"
        )
    );

    var totalRow = totalTable.insertRow(2);
    var totalLabelCell = totalRow.insertCell(0);
    totalLabelCell.appendChild(
        document.createTextNode(
            "Total:"
        )
    );
    var total = subTotal + 6;
    var totalCell = totalRow.insertCell(1);
    totalCell.appendChild(
        document.createTextNode(
            '$' + total.toString()
        )
    );

});