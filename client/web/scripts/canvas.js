const canvas = document.getElementById("map")
const mousePreview = document.getElementById("mouse-preview")
const color1 = document.getElementById("color-1")
const color2 = document.getElementById("color-2")

const ctx = canvas.getContext("2d")
const ctxM = mousePreview.getContext("2d")

const screen = document.getElementById("screen")

canvas.width = 1000;
canvas.height = 1000;
mousePreview.width = 1000;
mousePreview.height = 1000;

ctx.imageSmoothingEnabled = false;
ctxM.imageSmoothingEnabled = false;

ctx.fillStyle = "#FFF"
ctx.fillRect(0, 0, 1000, 1000)

toolassist = {size: 1}

pixelapp = new PixelApp(color1)

const pencil = new Pencil(ctx)
const eraser = new Eraser(ctx)
const pan = new Pan(ctx)
const bucket = new Bucket(ctx)
const dropper = new Dropper(ctx)
let lasttool = null

pixelapp.setTool(pencil)

color1.addEventListener("input", ()=>{
    pixelapp.setColor(color1.value)
})

const panzoom = Panzoom(screen, {
        maxScale: 100,
        minScale: 1,
        canvas: true,
        disablePan: true
    })

panzoom.pan(10, 10)
panzoom.zoom(3, { animate: true })

function disableButtons() {
    const botoes = document.querySelectorAll(".tools-container button");
    botoes.forEach(botao => botao.removeAttribute("on"));
}

function activateButton(str) {
    document.getElementsByClassName(str)[0].toggleAttribute("on", true)
}

function setPencil() {
    panzoom.setOptions({ disablePan: true })
    pixelapp.setTool(pencil)
    
    disableButtons()
    activateButton("pencil")
}

function setEraser() {
    panzoom.setOptions({ disablePan: true })
    pixelapp.setTool(eraser)
    disableButtons()
    activateButton("eraser")
}

function setDropper() {
    disableButtons()
    pixelapp.setTool(dropper)
    activateButton("dropper")
}

function setBucket() {
    disableButtons()
    pixelapp.setTool(bucket)
    activateButton("bucket")
}

function setPan() {
    panzoom.setOptions({ disablePan: false })
    pixelapp.setTool(pan)
    disableButtons()
    activateButton("pan")
}

function zoomIn() {
    panzoom.zoomIn()
}

function zoomOut() {
    panzoom.zoomOut();
}

screen.parentElement.addEventListener('wheel', panzoom.zoomWithWheel)

function getMousePos(canvas, event) {
    var rect = canvas.getBoundingClientRect();
    return {
        x: (event.clientX - rect.left) / (rect.right - rect.left) * canvas.width,
        y: (event.clientY - rect.top) / (rect.bottom - rect.top) * canvas.height
    };
}


let painting = false // mover para a maioria da lógica das ferramentas para tools.js
let old = { x: 0, y: 0 };

screen.addEventListener('pointermove', function(event) {
    const mousePos = getMousePos(canvas, event);
    
    const size = toolassist.size;
    const gridPos = {
        x: mousePos.x - (mousePos.x % size),
        y: mousePos.y - (mousePos.y % size)
    };
    
    ctxM.clearRect(old.x, old.y, size, size);
    
    
    ctxM.fillStyle = "rgba(0,0,0,0.5)"
    ctxM.fillRect(gridPos.x, gridPos.y, size, size);
    ctxM.fillStyle = "rgba(255, 255, 255, 0.5)"
    ctxM.fillRect(gridPos.x, gridPos.y, size, size);
    
    if (painting) {
        pixelapp.useTool(gridPos.x, gridPos.y, 0, 0)
    }

    old = gridPos;
});

let teste = ""
screen.addEventListener("pointerdown", function(event) {    
    painting = true
    // TODO: função pintar na tools.js feedback local
    const mousePos = getMousePos(canvas, event); 
    
    const size = toolassist.size;
    const gridPos = {
        x: mousePos.x - (mousePos.x % size),
        y: mousePos.y - (mousePos.y % size)
    };
    
    if (event.button === 2 && pixelapp.tool.name != "dropper") {
        teste = pixelapp.tool.name
        setDropper()
    }

    pixelapp.useTool(gridPos.x, gridPos.y, 0, 0)
});

screen.addEventListener("pointerup", function(event) {    
    painting = false
    if (event.button === 2) {
        if (teste === "pencil") {
            setPencil()
        } else {
            setBucket()
        }
    }
    pixelapp.upTool()
});

screen.addEventListener("pointerout", function(event) {    
    painting = false
    pixelapp.upTool()
});

setPan()

const colorLookup = {
    blue: "#3772FF",
    orange: "#f0803c",
    red: "#ef2d56",
    yellow: "rgb(236, 216, 29)",
    black: "#000",
    white: "#FFF",
    gray: "#8228ac",
    green: "#2fbf71",
    brown: "#b6531a",
    magenta: "#bf2fa7"
};

const palette = document.querySelectorAll(".mini-color");

for (const btn of palette) {
    btn.addEventListener("pointerdown", (event) => {
        console.log("hello")
        const clickedButton = event.currentTarget;
        
        let colorName = null;
        for (const className of clickedButton.classList) {
            if (colorLookup[className]) {
                colorName = className;
                break;
            }
        }

        if (colorName) {
            const colorValue = colorLookup[colorName];
            pixelapp.setColor(colorValue)
            color1.value = colorValue
        }
    });
}
