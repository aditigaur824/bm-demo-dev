function handleSearch() {
	const input = document.getElementById('search-input');
	console.log(`User query: ${input.value}`);
  
    const context = btoa(input);
    window.bmwidget.init(document.getElementById('chatBtn'), {
        'class': 'bmBtn',
        'context': context
    });
}