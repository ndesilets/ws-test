console.log('Hello, world!');

// Classes

class JobRequest {
  constructor(name) {
    this.name = name;
  }
}

// Util

function genRandomName() {
  return Math.round((Math.random() * Math.pow(10, 16))).toString(16);
}

// Init ws

const socket = new SockJS('/wat');
const stompClient = Stomp.over(socket);

stompClient.connect({}, (frame) => {
  console.log("Connected: " + frame);

  stompClient.subscribe('/jobs/pending', (message) => {
    const body = JSON.parse(message.body);
    console.log('on message /jobs/pending: ', body);

    const newPendingJobEl = document.createElement("li");
    newPendingJobEl.className = 'list-group-item d-flex justify-content-between align-items-center';
    newPendingJobEl.textContent = body.message;
    newPendingJobEl.id = body.message;

    pendingJobsListEl.appendChild(newPendingJobEl);
  });

  stompClient.subscribe('/jobs/completed', (message) => {
    const body = JSON.parse(message.body);
    console.log('on message /jobs/completed: ', body);

    const newCompletedJobEl = document.createElement("li");
    newCompletedJobEl.className = 'list-group-item d-flex justify-content-between align-items-center';
    newCompletedJobEl.textContent = body.message;
    newCompletedJobEl.id = body.message

    for (let child of pendingJobsListEl.childNodes) {
      if (child.textContent == body.message) {
        const node = document.getElementById(body.message);
        pendingJobsListEl.removeChild(node);
      }
    }

    completedJobsListEl.appendChild(newCompletedJobEl);
  });

  stompClient.subscribe('/jobs/failed', (message) => {
    const body = message.body;
    console.log('on message /jobs/failed: ', JSON.parse(body));

    const newFailedJobEl = document.createElement("li");
    newFailedJobEl.classList.add(['list-group-item', 'd-flex', 'justify-content-between', 'align-items-center'])
  });
});

// Init page

const jobNameEl = document.getElementById('job-name');
const newJobButtonEl = document.getElementById('submit-job');
const pendingJobsListEl = document.getElementById('pending-jobs-list');
const completedJobsListEl = document.getElementById('completed-jobs-list');

newJobButtonEl.addEventListener('click', () => {
  const name = jobNameEl.value || genRandomName();

  stompClient.send('/app/jobs/new', {priority: 9}, JSON.stringify(new JobRequest(name)));

  jobNameEl.value = null;
}, false);
