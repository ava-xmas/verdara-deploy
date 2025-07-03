document.addEventListener('DOMContentLoaded', () => {
  const grid = document.getElementById('pixel-grid');
  const halfCols = 150;
  for (let iy = 0; iy < 400; iy++) {
    for (let ix = 0; ix < 300; ix++) {
      const x = ix - halfCols;
      const y = 400 - iy;
      const div = document.createElement('div');
      div.className = 'pixel';
      div.id = `pixel-[${x}]-[${y}]`;
      grid.appendChild(div);
    }
  }

  const generate = async () => {
    const prompt = document.getElementById('prompt').value;
    document.querySelectorAll('.pixel')
      .forEach(p => p.style.background = '#fff');
    try {
      const res = await fetch('http://localhost:8080/api/prompt', {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain' },
        body: prompt
      });
      if (!res.ok) throw new Error(res.status);
      const data = await res.json();
      data.forEach(({ x, y, shade }) => {
        const el = document.getElementById(`pixel-[${x}]-[${y}]`);
        if (el) el.style.background = shade;
      });
    } catch (err) {
      alert('Error: ' + err);
    }
  }

  
  const promptInput = document.getElementById('prompt');
  const generateBtn = document.getElementById('generate');
  
  generateBtn.addEventListener('click', generate);
  promptInput.addEventListener('keydown', function(event) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      generateBtn.click();
    }
  });
});
