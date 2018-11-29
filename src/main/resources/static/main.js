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

  stompClient.subscribe('/jobs/status', (message) => {
    const body = message.body;
    console.log('on message /jobs/status: ', JSON.parse(body));
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
