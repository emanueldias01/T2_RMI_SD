const api_url = ""
const socket = new WebSocket("ws://localhost:3001")

var pixelsize = 0
var pixelbuffer = []

socket.onopen = () => {
    socket.send(JSON.stringify(pixelbuffer));
};

setInterval(() => {
    if (pixelsize >= 1) {
        socket.send(JSON.stringify(pixelbuffer))
        pixelsize = 0
        pixelbuffer = []
        return
    }
}, 200)

const modal = document.getElementById("modal")
const modal2 = document.getElementById("modal2")

document.getElementById('login-form').addEventListener('submit', async function(event) {
    event.preventDefault(); 

    const usernameInput = document.getElementById('username').value;
    const passwordInput = document.getElementById('password').value;
    const submitButton = document.querySelector('.form-field button');

    modal.style.display = "none";
});


document.getElementById('singup-form').addEventListener('submit', async function(event) {
    event.preventDefault(); 

    const usernameInput = document.getElementById('username').value;
    const passwordInput = document.getElementById('password').value;
    const submitButton = document.querySelector('.form-field button');
    
    modal2.style.display = "none";
});

document.getElementById('btn-singup').addEventListener('click', function() {
    document.getElementById('modal2').style.display = 'flex';
    document.getElementById('modal').style.display = 'none';
});

document.getElementById('btn-login').addEventListener('click', function() {
    document.getElementById('modal').style.display = 'flex';
    document.getElementById('modal2').style.display = 'none';
});
