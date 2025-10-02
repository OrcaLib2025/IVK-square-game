const apiBase = '/api/squares';
let size = 5;
let board = [];
let nextColor = 'b';

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('new').addEventListener('click', () => {
        size = parseInt(document.getElementById('size').value) || 5;
        startNew();
    });
    startNew();
});

function startNew() {
    board = Array(size).fill(null).map(()=>Array(size).fill(' '));
    nextColor = 'b';
    renderBoard();
    setStatus('New game');
}

function renderBoard() {
    const b = document.getElementById('board');
    b.innerHTML = '';
    b.style.gridTemplateColumns = `repeat(${size}, 48px)`;
    for (let y=0;y<size;y++){
        for (let x=0;x<size;x++){
            const div = document.createElement('div');
            div.className = 'cell';
            div.dataset.x = x.toString(); div.dataset.y = y.toString();
            div.innerText = cellSymbol(board[y][x]);
            div.addEventListener('click', onCellClick);
            b.appendChild(div);
        }
    }
}

function cellSymbol(c){
    if (c === 'w') return '○';
    if (c === 'b') return '●';
    return '';
}

async function onCellClick(e){
    const x = parseInt(e.currentTarget.dataset.x);
    const y = parseInt(e.currentTarget.dataset.y);
    if (board[y][x] !== ' ') return;
    board[y][x] = 'w';
    renderBoard();
    setStatus('You moved. Checking status...');
    const status = await postJson(apiBase + '/gameStatus', buildBoardDto('b'));
    setStatus('Server: ' + (status && status.message ? status.message : 'no response'));
    if (status && status.status === 1) { alert('Winner: ' + status.color); return; }
    if (status && status.status === 2) { alert('Draw'); return; }

    const moveResp = await postJson(apiBase + '/nextMove', buildBoardDto('b'));
    if (moveResp) {
        board[moveResp.y][moveResp.x] = moveResp.color;
        renderBoard();
        const status2 = await postJson(apiBase + '/gameStatus', buildBoardDto('w'));
        if (status2 && status2.status === 1) {
            alert('Winner: ' + status2.color);
            return;
        }
        if (status2 && status2.status === 2) {
            alert('Draw');
        }
    } else {
        setStatus('No move from server (maybe game finished)');
    }
}

function buildBoardDto(nextPlayerColor){
    let s = '';
    for (let y=0;y<size;y++){
        for (let x=0;x<size;x++) s += board[y][x];
    }
    return { size: size, data: s, nextPlayerColor: nextPlayerColor };
}

async function postJson(url, payload){
    try {
        const r = await fetch(url, {
            method: 'POST',
            headers: {'Content-Type':'application/json'},
            body: JSON.stringify(payload)
        });
        if (r.status === 204) return null;
        return await r.json();
    } catch (err) {
        console.error(err);
        setStatus('Network error');
        return null;
    }
}

function setStatus(s){ document.getElementById('status').innerText = 'status: ' + s; }
