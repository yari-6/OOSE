document.addEventListener("DOMContentLoaded", function() {
   const patternSelect = document.getElementById('pattern');
   const wrap = document.getElementById('sessionsWrap');
   const siForm = document.getElementById('siForm');

   const fmt = m => {
     let h = Math.floor(m / 60);
     let mm = String(m % 60).padStart(2, '0');
     let suffix = h >= 12 ? 'PM' : 'AM';
     let displayH = h % 12 || 12;
     return `${displayH}:${mm} ${suffix}`;
   };

   function renderSessions() {
     wrap.innerHTML = '';
     const [count, dur] = patternSelect.value.split('x').map(Number);

     for(let i=0; i<count; i++) {
        const div = document.createElement('div');
        div.className = 'session-row fade-in';

        div.innerHTML = `
          <span>Session ${i+1}:</span>
          <select name="sessions[${i}].day" class="day-select" required>
            <option value="M">Monday</option><option value="T">Tuesday</option>
            <option value="W">Wednesday</option><option value="R">Thursday</option>
            <option value="F">Friday</option>
          </select>
          <select name="sessions[${i}].startMinutes" class="time-select" required></select>
          <span class="end-display"></span>
        `;

        const timeSel = div.querySelector('.time-select');
        const endDisp = div.querySelector('.end-display');

        for(let t = 660; t <= 1080; t += 15) {
          const opt = document.createElement('option');
          opt.value = t; opt.textContent = fmt(t);
          timeSel.appendChild(opt);
        }

        timeSel.addEventListener('change', () => {
          endDisp.textContent = " — Ends at " + fmt(Number(timeSel.value) + dur);
        });

        timeSel.dispatchEvent(new Event('change'));
        wrap.appendChild(div);
        }
     }

     siForm.addEventListener('submit', (e) => {
         const rows = wrap.querySelectorAll('.session-row');
         const seen = new Set();

         for (const row of rows) {
              const day = row.querySelector('.day-select').value;
              const time = row.querySelector('.time-select').value;
              const comboKey = `${day}-${time}`;

              if (seen.has(comboKey)) {
                  e.preventDefault();
                  const dayName = row.querySelector('.day-select').selectedOptions[0].text;
                  alert(`Overlap Error: You have multiple sessions scheduled for ${dayName} at ${fmt(Number(time))}.`);
                  return;
              }
              seen.add(comboKey);
         }
     });

     patternSelect.addEventListener('change', renderSessions);

     renderSessions();

     let existingData = [];
     const rawData = wrap.getAttribute('data-existing');

     if (rawData && rawData !== '[]' && rawData !== '') {
         try {
             existingData = JSON.parse(rawData);
         } catch (e) {
             console.error("Error parsing schedule data:", e);
         }
     }

     if (existingData && existingData.length > 0) {
       const count = existingData.length;
       if (count === 3) patternSelect.value = "3x75";
       if (count === 4) patternSelect.value = "4x50";

       renderSessions();

       const rows = wrap.querySelectorAll('.session-row');
       existingData.forEach((session, index) => {
         if (rows[index]) {
           rows[index].querySelector('.day-select').value = session.sessionID.day;
           const [h, m] = session.sessionID.time.split(':');
           rows[index].querySelector('.time-select').value = (parseInt(h) * 60) + parseInt(m);
           rows[index].querySelector('.time-select').dispatchEvent(new Event('change'));
         }
       });
     }
});