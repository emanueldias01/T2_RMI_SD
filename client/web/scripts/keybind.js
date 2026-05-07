document.addEventListener("keydown", (e) => {
    // O(1) \o/
    switch(e.key.toLowerCase()) {
        case 'w':
        case 'b':
            setPencil();
            break;
        case 'e':
            setEraser();
            break;
        case 'r':
        case 'g':
            setBucket();
            break;
        case 'i':
        case 'k':
            setDropper();
            break;
        case 'h':
        case ' ':
            setPan();
            break;
        case '+':
        case '=':
            zoomIn();
            break;
        case '-':
        case '_':
            zoomOut();
            break;
        case '1':
            pixelapp.setColor(colorLookup.blue)
            break;
        case '2':
            pixelapp.setColor(colorLookup.yellow);
            break;
        case '3':
            pixelapp.setColor(colorLookup.orange);
            break;
        case '4':
            pixelapp.setColor(colorLookup.red);
            break;
        case '5':
            pixelapp.setColor();
            break;
        case '6':
            pixelapp.setColor();
            break;
        case '7':
            pixelapp.setColor();
            break;
        case '8':
            pixelapp.setColor();
            break;
        case '9':
            pixelapp.setColor();
            break;
        case '0':
            pixelapp.setColor();
            break;
    }
})