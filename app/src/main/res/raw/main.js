const observer = new MutationObserver( _ => {
    window.location.href = "change://" + document.getElementById('editor').innerHTML
});
observer.observe(document.getElementById('editor'), { childList: true, characterData: true, subtree: true });

function changeFontSize(target) {
    const selected = window.getSelection().getRangeAt(0);
    const selectedText = selected.extractContents();
    const change = document.createElement(target);
    change.appendChild(selectedText);
    selected.insertNode(change);
}
