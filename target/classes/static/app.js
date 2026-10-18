// DocuSense AI Frontend Controller
let currentSessionId = null;
let selectedFile = null;
let cachedDocuments = [];

document.addEventListener('DOMContentLoaded', () => {
    initTabs();
    initDropZone();
    loadDocuments();
    loadDashboardStats();
});

// Tab Navigation
function initTabs() {
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        item.addEventListener('click', () => {
            const tabId = item.getAttribute('data-tab');
            navItems.forEach(btn => btn.classList.remove('active'));
            document.querySelectorAll('.tab-pane').forEach(pane => pane.classList.remove('active'));

            item.classList.add('active');
            const targetPane = document.getElementById('tab-' + tabId);
            if (targetPane) targetPane.classList.add('active');

            if (tabId === 'documents') loadDocuments();
            if (tabId === 'database') loadDbInspection();
            if (tabId === 'chat') loadDocumentsDropdown(cachedDocuments);
        });
    });
}

// Quick Questions
function askQuickQuestion(text) {
    document.getElementById('questionInput').value = text;
    handleSendQuestion(new Event('submit'));
}

// Chat Functionality
async function handleSendQuestion(e) {
    if (e) e.preventDefault();
    const input = document.getElementById('questionInput');
    const question = input.value.trim();
    if (!question) return;

    input.value = '';
    appendMessage('USER', question);

    const docFilter = document.getElementById('documentFilterSelect').value;
    const docId = docFilter ? parseInt(docFilter) : null;

    // Show loading assistant placeholder
    const loadingMsgId = appendMessage('ASSISTANT', '<div class="typing-indicator">Searching documents & generating answer...</div>', null, true);

    try {
        const response = await fetch('/api/chat/ask', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                question: question,
                sessionId: currentSessionId,
                documentId: docId,
                topK: 4
            })
        });

        if (!response.ok) {
            throw new Error('Server returned ' + response.status);
        }

        const data = await response.json();
        currentSessionId = data.sessionId;

        // Replace loading message with actual response
        updateAssistantMessage(loadingMsgId, data.answer, data.sources);
    } catch (err) {
        updateAssistantMessage(loadingMsgId, '⚠️ Error connecting to server: ' + err.message);
    }
}

function appendMessage(role, text, sources = null, isLoading = false) {
    const container = document.getElementById('messagesContainer');
    const msgId = 'msg-' + Date.now();

    const msgDiv = document.createElement('div');
    msgDiv.id = msgId;
    msgDiv.className = `message ${role.toLowerCase()}`;

    const avatar = role === 'USER' ? '👤' : '🤖';
    const sender = role === 'USER' ? 'You' : 'DocuSense AI (Assistant)';

    let sourcesHtml = '';
    if (sources && sources.length > 0) {
        sourcesHtml = `
            <div class="sources-container">
                <div class="sources-header">📚 Retrieved Document Context (${sources.length} chunks)</div>
                ${sources.map(s => `
                    <div class="source-item">
                        <div class="source-title">📄 ${escapeHtml(s.documentName)} (Chunk #${s.chunkIndex})</div>
                        <div>${escapeHtml(s.snippet)}</div>
                    </div>
                `).join('')}
            </div>
        `;
    }

    msgDiv.innerHTML = `
        <div class="avatar">${avatar}</div>
        <div class="bubble">
            <div class="sender-name">${sender}</div>
            <div class="text">${isLoading ? text : formatMarkdown(text)}</div>
            ${sourcesHtml}
        </div>
    `;

    container.appendChild(msgDiv);
    container.scrollTop = container.scrollHeight;
    return msgId;
}

function updateAssistantMessage(msgId, text, sources = null) {
    const msgDiv = document.getElementById(msgId);
    if (!msgDiv) return;

    let sourcesHtml = '';
    if (sources && sources.length > 0) {
        sourcesHtml = `
            <div class="sources-container">
                <div class="sources-header">📚 Retrieved Context (${sources.length} chunks)</div>
                ${sources.map(s => `
                    <div class="source-item">
                        <div class="source-title">📄 ${escapeHtml(s.documentName)} (Chunk #${s.chunkIndex})</div>
                        <div>${escapeHtml(s.snippet)}</div>
                    </div>
                `).join('')}
            </div>
        `;
    }

    const bubble = msgDiv.querySelector('.bubble');
    bubble.innerHTML = `
        <div class="sender-name">DocuSense AI (Assistant)</div>
        <div class="text">${formatMarkdown(text)}</div>
        ${sourcesHtml}
    `;

    const container = document.getElementById('messagesContainer');
    container.scrollTop = container.scrollHeight;
}

// Drag & Drop Document Handling
function initDropZone() {
    const dropZone = document.getElementById('dropZone');
    ['dragenter', 'dragover'].forEach(name => {
        dropZone.addEventListener(name, (e) => {
            e.preventDefault();
            dropZone.classList.add('dragover');
        });
    });
    ['dragleave', 'drop'].forEach(name => {
        dropZone.addEventListener(name, (e) => {
            e.preventDefault();
            dropZone.classList.remove('dragover');
        });
    });
    dropZone.addEventListener('drop', (e) => {
        if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
            handleFile(e.dataTransfer.files[0]);
        }
    });
}

function handleFileSelected(e) {
    if (e.target.files && e.target.files.length > 0) {
        handleFile(e.target.files[0]);
    }
}

function handleFile(file) {
    selectedFile = file;
    document.getElementById('selectedFileName').textContent = `Selected: ${file.name} (${(file.size / 1024).toFixed(1)} KB)`;
    document.getElementById('uploadBtn').style.display = 'block';
}

async function uploadDocument() {
    if (!selectedFile) return;

    const btn = document.getElementById('uploadBtn');
    const progress = document.getElementById('uploadProgress');
    btn.disabled = true;
    progress.style.display = 'block';

    const formData = new FormData();
    formData.append('file', selectedFile);

    try {
        const response = await fetch('/api/documents/upload', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.error || 'Upload failed');
        }

        const doc = await response.json();
        alert(`Document "${doc.fileName}" indexed successfully into ${doc.chunkCount} vector chunks!`);
        selectedFile = null;
        document.getElementById('fileInput').value = '';
        document.getElementById('selectedFileName').textContent = '';
        btn.style.display = 'none';

        loadDocuments();
        loadDashboardStats();
    } catch (err) {
        alert('Upload Error: ' + err.message);
    } finally {
        btn.disabled = false;
        progress.style.display = 'none';
    }
}

// Document List
async function loadDocuments() {
    try {
        const res = await fetch('/api/documents');
        const docs = await res.json();
        const tbody = document.getElementById('documentTableBody');
        const pill = document.getElementById('docCountPill');
        if (pill) pill.textContent = docs.length;

        if (docs.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center">No documents uploaded yet.</td></tr>';
            return;
        }

        tbody.innerHTML = docs.map(d => `
            <tr>
                <td><strong>#${d.id}</strong></td>
                <td><strong>📄 ${escapeHtml(d.fileName)}</strong></td>
                <td>${(d.fileSizeBytes / 1024).toFixed(1)} KB</td>
                <td><span class="status-badge ${d.status}">${d.status}</span></td>
                <td><strong>${d.chunkCount || 0}</strong> chunks</td>
                <td>${formatDate(d.uploadedAt)}</td>
                <td>
                    <button class="btn btn-sm btn-secondary" onclick="deleteDoc(${d.id})" style="color: #ef4444;">Delete</button>
                </td>
            </tr>
        `).join('');

        cachedDocuments = docs;
        loadDocumentsDropdown(docs);
    } catch (e) {
        console.error('Failed to load documents', e);
    }
}

function loadDocumentsDropdown(docs) {
    const select = document.getElementById('documentFilterSelect');
    if (!select) return;

    const list = (docs && docs.length > 0) ? docs : cachedDocuments;
    const currentVal = select.value;
    select.innerHTML = '<option value="">All Indexed Documents (Global Search)</option>';

    if (list && list.length > 0) {
        list.forEach(d => {
            const opt = document.createElement('option');
            opt.value = d.id;
            opt.textContent = `📄 ${d.fileName} (${d.chunkCount} chunks)`;
            select.appendChild(opt);
        });
        if (currentVal) select.value = currentVal;
    }
}

async function deleteDoc(id) {
    if (!confirm('Are you sure you want to delete this document and its vector embeddings?')) return;
    try {
        await fetch('/api/documents/' + id, { method: 'DELETE' });
        loadDocuments();
        loadDashboardStats();
    } catch (e) {
        alert('Delete failed');
    }
}

// Hibernate Database Inspection
async function loadDbInspection() {
    try {
        const res = await fetch('/api/dashboard/inspect-database');
        const data = await res.json();

        document.getElementById('statUsers').textContent = data.totalUsersInDb || 0;
        document.getElementById('statDocs').textContent = data.totalDocumentsInDb || 0;
        document.getElementById('statMessages').textContent = data.totalChatMessagesInDb || 0;

        const tbody = document.getElementById('messagesTableBody');
        const msgs = data.recentMessages || [];

        if (msgs.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center">No chat messages in database yet.</td></tr>';
            return;
        }

        tbody.innerHTML = msgs.map(m => `
            <tr>
                <td>#${m.id}</td>
                <td><strong>${m.role}</strong></td>
                <td>${escapeHtml(m.content ? m.content.substring(0, 100) : '')}${m.content && m.content.length > 100 ? '...' : ''}</td>
                <td>${formatDate(m.createdAt)}</td>
            </tr>
        `).join('');
    } catch (e) {
        console.error('Failed to load DB inspection', e);
    }
}

// Dashboard Stats
async function loadDashboardStats() {
    try {
        const res = await fetch('/api/dashboard/stats');
        const stats = await res.json();
        document.getElementById('statChunks').textContent = stats.totalChunks || 0;
    } catch (e) {}
}

// Utility Helpers
function escapeHtml(text) {
    if (!text) return '';
    return text.replace(/&/g, '&amp;')
               .replace(/</g, '&lt;')
               .replace(/>/g, '&gt;');
}

function formatMarkdown(text) {
    if (!text) return '';
    return text
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.*?)\*/g, '<em>$1</em>')
        .replace(/\n\n/g, '<br><br>')
        .replace(/\n/g, '<br>');
}

function formatDate(isoStr) {
    if (!isoStr) return '-';
    try {
        const d = new Date(isoStr);
        return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) + ' ' + d.toLocaleDateString();
    } catch (e) {
        return isoStr;
    }
}
