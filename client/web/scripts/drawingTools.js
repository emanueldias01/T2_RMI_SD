class PixelApp {
    constructor(color) {
        this.colorSlot = 0
        this.color = color
        this.tool = null
    }

    setColor(color) {
        this.color.value = color
    }

    setTool(tool) {
        this.tool = tool
    }

    useTool(x, y, x1, y2) {
        const result = this.tool.use(this.color.value, x, y, x1, y2)

        if (typeof result === 'string') {
            this.setColor(result)
        }
    }

    upTool() {
        this.tool.up()
    }
}

class Pencil {
    constructor(ctx) {
        this.size = 1
        this.ctx = ctx
        this.name = "pencil"
        this.lastX = null
        this.lastY = null
    }

    use(color, x, y, _, _1) {
        this.ctx.fillStyle = color;

        if (this.lastX == x && this.lastY == y) {
            return
        }

        if (this.lastX == null || this.lastY == null) {
            this.lastX = x
            this.lastY = y
        }

        let gridX0 = Math.floor(this.lastX / this.size);
        let gridY0 = Math.floor(this.lastY / this.size);
        let gridX1 = Math.floor(x / this.size);
        let gridY1 = Math.floor(y / this.size);

        let dx = Math.abs(gridX1 - gridX0);
        let dy = Math.abs(gridY1 - gridY0);
        let sx = (gridX0 < gridX1) ? 1 : -1;
        let sy = (gridY0 < gridY1) ? 1 : -1;
        let err = dx - dy;

        while (true) {
            this.ctx.fillRect(gridX0 * this.size, gridY0 * this.size, this.size, this.size);
            
            pixelbuffer.push([color, gridX0, gridY0])
            pixelsize += 1

            if (gridX0 === gridX1 && gridY0 === gridY1) break;

            let e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                gridX0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                gridY0 += sy;
            }
        }

        this.lastX = x;
        this.lastY = y;
    }

    up() {
        this.lastX = null;
        this.lastY = null;
    }

    changeSize(size) {
        this.size = size
    }
}

class Eraser {
    constructor(ctx) {
        this.name = "eraser"
        this.size = 1
        this.ctx = ctx
        this.lastX = null
        this.lastY = null
    }

    use(color, x, y, x1, y2) {
        this.ctx.fillStyle = "#FFF";

        if (this.lastX == null || this.lastY == null) {
            this.lastX = x
            this.lastY = y
        }

        let gridX0 = Math.floor(this.lastX / this.size);
        let gridY0 = Math.floor(this.lastY / this.size);
        let gridX1 = Math.floor(x / this.size);
        let gridY1 = Math.floor(y / this.size);

        let dx = Math.abs(gridX1 - gridX0);
        let dy = Math.abs(gridY1 - gridY0);
        let sx = (gridX0 < gridX1) ? 1 : -1;
        let sy = (gridY0 < gridY1) ? 1 : -1;
        let err = dx - dy;

        while (true) {
            this.ctx.fillRect(gridX0 * this.size, gridY0 * this.size, this.size, this.size);
            pixelbuffer.push([gridX0, gridY0])
            pixelsize += 1
            if (gridX0 === gridX1 && gridY0 === gridY1) break;

            let e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                gridX0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                gridY0 += sy;
            }
        }

        this.lastX = x;
        this.lastY = y;
    }

    up() {
        this.lastX = null;
        this.lastY = null;
    }

    changeSize(size) {
        this.size = size
    }
}

class Pan {
    constructor(ctx) {
        this.name = "pan"
        this.size = 10
        this.ctx = ctx
    }

    use(color, x, y, x1, y2) {

    }
}

class Dropper {
    constructor(ctx) {
        this.name = "dropper"
        this.size = 1
        this.ctx = ctx
    }

    use(color, x, y, x1, y2) {
        const pixel = ctx.getImageData(x, y, 1, 1)
        const [r, g, b, a] = pixel.data
        
        const rgbaColor = `rgba(${r}, ${g}, ${b}, ${a / 255})`;
        console.log(rgbaColor)
        return rgbaColor
    }
}

class Bucket {
    constructor(ctx) {
        this.name = "bucket"
        this.size = 1
        this.ctx = ctx
    }

    use(color, x, y, x1, y2) {
        
    }
}