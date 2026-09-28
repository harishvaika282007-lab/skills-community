/**
 * SkillSwap Dashboard SPA JavaScript
 * Communicates with Spring Boot REST endpoints on localhost:8080.
 */

// Global State
let membersCache = [];
let skillsCache = [];
let sessionsCache = [];
let ledgerCache = [];
let statusChart = null;
let categoryChart = null;

// Initialize on DOM load
document.addEventListener('DOMContentLoaded', () => {
    setupNavigation();
    loadAllData();

    // Auto-refresh hook
    const btnRefresh = document.getElementById('btn-refresh');
    if (btnRefresh) {
        btnRefresh.addEventListener('click', () => {
            loadAllData();
            showToast('Data refreshed successfully', 'info');
        });
    }
});

// ─── Tab Navigation ────────────────────────────────────────────────────────
function setupNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const tabName = item.getAttribute('data-tab');
            switchTab(tabName);
        });
    });
}

function switchTab(tabName) {
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.toggle('active', item.getAttribute('data-tab') === tabName);
    });

    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.toggle('active', content.id === `tab-${tabName}`);
    });

    // Update titles
    const titles = {
        overview: { title: 'Dashboard Overview', sub: 'Real-time statistics & activity monitoring' },
        members: { title: 'Community Members', sub: 'Manage registered users & time-credit balances' },
        skills: { title: 'Skill Offers Marketplace', sub: 'Browse and offer community skills' },
        sessions: { title: 'Session Requests & Workflow', sub: 'Track lifecycle: Requested → Confirmed → Completed' },
        ledger: { title: 'Credit Ledger Audit Trail', sub: 'Complete traceable history of all time-credit transactions' },
        'api-explorer': { title: 'Live REST APIs Console', sub: 'Directly execute, inspect, and test all Spring Boot backend endpoints' }
    };
    if (titles[tabName]) {
        document.getElementById('page-title').innerText = titles[tabName].title;
        document.getElementById('page-subtitle').innerText = titles[tabName].sub;
    }
}

// ─── Fetch All Data ────────────────────────────────────────────────────────
async function loadAllData() {
    await Promise.all([
        fetchSummary(),
        fetchActivityCharts(),
        fetchRecentData(),
        fetchMembers(),
        fetchSkills(),
        fetchSessions(),
        fetchLedger()
    ]);
    populateDropdowns();
    checkZeroState();
}

function checkZeroState() {
    const banner = document.getElementById('zero-state-banner');
    const skillWarning = document.getElementById('skill-member-empty-warning');
    const sessionWarning = document.getElementById('session-empty-warning');
    const btnSkillSubmit = document.getElementById('btn-submit-skill');
    const btnSessionSubmit = document.getElementById('btn-submit-session');

    if (membersCache.length === 0) {
        if (banner) banner.style.display = 'flex';
        if (skillWarning) skillWarning.style.display = 'flex';
        if (sessionWarning) sessionWarning.style.display = 'flex';
        if (btnSkillSubmit) btnSkillSubmit.disabled = true;
        if (btnSessionSubmit) btnSessionSubmit.disabled = true;
    } else {
        if (banner) banner.style.display = 'none';
        if (skillWarning) skillWarning.style.display = 'none';
        if (btnSkillSubmit) btnSkillSubmit.disabled = false;

        if (skillsCache.length === 0) {
            if (sessionWarning) sessionWarning.style.display = 'flex';
            if (btnSessionSubmit) btnSessionSubmit.disabled = true;
        } else {
            if (sessionWarning) sessionWarning.style.display = 'none';
            if (btnSessionSubmit) btnSessionSubmit.disabled = false;
        }
    }
}

// ─── 1-Click Seed Sample Community Data ────────────────────────────────────
async function handleSeedData() {
    const btn = document.getElementById('btn-seed-data');
    if (btn) btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Loading...';
    try {
        const res = await fetch('/api/dashboard/seed-sample-data', { method: 'POST' });
        const data = await res.json();
        showToast(data.message || 'Sample data loaded successfully!', 'success');
        await loadAllData();
    } catch (err) {
        showToast('Error seeding demo data', 'error');
    } finally {
        if (btn) btn.innerHTML = '<i class="fa-solid fa-wand-magic-sparkles"></i> <span>Load Sample Data</span>';
    }
}

// ─── 1. Dashboard Summary ──────────────────────────────────────────────────
async function fetchSummary() {
    try {
        const res = await fetch('/api/dashboard/summary');
        if (!res.ok) return;
        const data = await res.json();

        document.getElementById('kpi-members').innerText = data.totalMembers ?? 0;
        document.getElementById('kpi-skills').innerText = data.totalSkillOffers ?? 0;
        document.getElementById('kpi-pending').innerText = data.pendingSessions ?? 0;
        document.getElementById('kpi-confirmed').innerText = data.confirmedSessions ?? 0;
        document.getElementById('kpi-completed').innerText = data.completedSessions ?? 0;
        document.getElementById('kpi-credits').innerText = data.totalCreditsInCirculation ?? 0;
    } catch (err) {
        console.error('Error fetching summary:', err);
    }
}

// ─── 2. Activity Charts ────────────────────────────────────────────────────
async function fetchActivityCharts() {
    try {
        const res = await fetch('/api/dashboard/activity');
        if (!res.ok) return;
        const data = await res.json();

        // 1. Session Status Chart
        const statusData = data.sessionStatusDistribution || {};
        const statusLabels = Object.keys(statusData);
        const statusValues = Object.values(statusData);

        const ctxStatus = document.getElementById('chart-session-status').getContext('2d');
        if (statusChart) statusChart.destroy();
        statusChart = new Chart(ctxStatus, {
            type: 'doughnut',
            data: {
                labels: statusLabels,
                datasets: [{
                    data: statusValues.length ? statusValues : [1],
                    backgroundColor: ['#f59e0b', '#3b82f6', '#10b981', '#ef4444', '#64748b'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'bottom', labels: { color: '#94a3b8' } }
                }
            }
        });

        // 2. Skill Category Chart
        const catData = data.skillCategoryDistribution || {};
        const catLabels = Object.keys(catData);
        const catValues = Object.values(catData);

        const ctxCat = document.getElementById('chart-skill-categories').getContext('2d');
        if (categoryChart) categoryChart.destroy();
        categoryChart = new Chart(ctxCat, {
            type: 'bar',
            data: {
                labels: catLabels.length ? catLabels : ['General'],
                datasets: [{
                    label: 'Available Offers',
                    data: catValues.length ? catValues : [0],
                    backgroundColor: '#6366f1',
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: { beginAtZero: true, ticks: { color: '#94a3b8', stepSize: 1 } },
                    x: { ticks: { color: '#94a3b8' } }
                },
                plugins: { legend: { display: false } }
            }
        });
    } catch (err) {
        console.error('Error fetching activity:', err);
    }
}

// ─── 3. Recent Members & Sessions for Overview ─────────────────────────────
async function fetchRecentData() {
    try {
        const [resMembers, resSessions] = await Promise.all([
            fetch('/api/dashboard/recent-members'),
            fetch('/api/dashboard/recent-sessions')
        ]);

        if (resMembers.ok) {
            const members = await resMembers.json();
            const tbody = document.getElementById('tbody-recent-members');
            tbody.innerHTML = members.length === 0 
                ? '<tr><td colspan="4" class="text-center text-muted">No members registered yet</td></tr>'
                : members.map(m => `
                    <tr>
                        <td><strong>${escapeHtml(m.name)}</strong></td>
                        <td>${escapeHtml(m.email)}</td>
                        <td><span class="badge badge-credit">${m.creditBalance} hrs</span></td>
                        <td>
                            <button class="btn btn-secondary btn-sm" onclick="viewMemberDetails(${m.id})">
                                Details
                            </button>
                        </td>
                    </tr>
                `).join('');
        }

        if (resSessions.ok) {
            const sessions = await resSessions.json();
            const tbody = document.getElementById('tbody-recent-sessions');
            tbody.innerHTML = sessions.length === 0
                ? '<tr><td colspan="4" class="text-center text-muted">No session requests yet</td></tr>'
                : sessions.map(s => `
                    <tr>
                        <td>${escapeHtml(s.skillOffer?.skillName || 'N/A')}</td>
                        <td>${escapeHtml(s.requester?.name || 'N/A')}</td>
                        <td>${s.hoursRequested}h</td>
                        <td><span class="badge badge-status-${s.status.toLowerCase()}">${s.status}</span></td>
                    </tr>
                `).join('');
        }
    } catch (err) {
        console.error('Error fetching recent items:', err);
    }
}

// ─── 4. Members Table & Management ─────────────────────────────────────────
async function fetchMembers() {
    try {
        const res = await fetch('/api/members');
        if (!res.ok) return;
        membersCache = await res.json();
        renderMembersTable(membersCache);
    } catch (err) {
        console.error('Error fetching members:', err);
    }
}

function renderMembersTable(members) {
    const tbody = document.getElementById('tbody-members');
    if (!members.length) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No members found. Click "Register Member" or "Load Sample Data" above!</td></tr>';
        return;
    }
    tbody.innerHTML = members.map(m => `
        <tr>
            <td>#${m.id}</td>
            <td><strong>${escapeHtml(m.name)}</strong></td>
            <td>${escapeHtml(m.email)}</td>
            <td><span class="badge badge-credit">${m.creditBalance} credits</span></td>
            <td>
                <button class="btn-icon" title="View Full Member Profile & Activity" onclick="viewMemberDetails(${m.id})">
                    <i class="fa-solid fa-id-card"></i>
                </button>
                <button class="btn-icon" title="View Credit Ledger" onclick="viewMemberLedger(${m.id})">
                    <i class="fa-solid fa-receipt"></i>
                </button>
                <button class="btn-icon danger" title="Delete Member" onclick="handleDeleteMember(${m.id})">
                    <i class="fa-solid fa-trash"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

function filterMembersTable() {
    const q = document.getElementById('member-search').value.toLowerCase();
    const filtered = membersCache.filter(m => 
        m.name.toLowerCase().includes(q) || m.email.toLowerCase().includes(q)
    );
    renderMembersTable(filtered);
}

async function handleCreateMember(e) {
    e.preventDefault();
    const name = document.getElementById('member-name').value.trim();
    const email = document.getElementById('member-email').value.trim();

    try {
        const res = await fetch('/api/members', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email })
        });
        const data = await res.json();
        if (res.ok) {
            showToast(`Member '${data.name}' registered successfully!`, 'success');
            closeModal('modal-member');
            document.getElementById('form-member').reset();
            loadAllData();
        } else {
            showToast(data.message || data.error || 'Failed to create member', 'error');
        }
    } catch (err) {
        showToast('Network error while registering member', 'error');
    }
}

async function handleDeleteMember(id) {
    if (!confirm(`Are you sure you want to delete member #${id}?`)) return;
    try {
        const res = await fetch(`/api/members/${id}`, { method: 'DELETE' });
        if (res.ok) {
            showToast('Member deleted', 'info');
            loadAllData();
        } else {
            const data = await res.json();
            showToast(data.message || 'Cannot delete member (may have active sessions/offers)', 'error');
        }
    } catch (err) {
        showToast('Error deleting member', 'error');
    }
}

function viewMemberDetails(memberId) {
    const member = membersCache.find(m => m.id === memberId);
    if (!member) return;

    // Filter skills and sessions for this member
    const offeredSkills = skillsCache.filter(s => s.provider?.id === memberId);
    const requestedSessions = sessionsCache.filter(s => s.requester?.id === memberId);
    const providedSessions = sessionsCache.filter(s => s.skillOffer?.provider?.id === memberId);

    const body = document.getElementById('member-details-body');
    body.innerHTML = `
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
            <div>
                <h4 style="font-size:1.2rem; font-weight:800;">${escapeHtml(member.name)}</h4>
                <div style="color:var(--text-secondary); font-size:0.85rem;">${escapeHtml(member.email)}</div>
            </div>
            <span class="badge badge-credit" style="font-size:0.9rem; padding:6px 14px;">
                ${member.creditBalance} Time Credits
            </span>
        </div>

        <div style="display:grid; grid-template-columns:1fr 1fr 1fr; gap:10px; margin-bottom:20px;">
            <div style="background:rgba(255,255,255,0.03); border:1px solid var(--border-color); border-radius:8px; padding:12px; text-align:center;">
                <div style="font-size:1.3rem; font-weight:800; color:#818cf8;">${offeredSkills.length}</div>
                <small style="color:var(--text-muted);">Skills Offered</small>
            </div>
            <div style="background:rgba(255,255,255,0.03); border:1px solid var(--border-color); border-radius:8px; padding:12px; text-align:center;">
                <div style="font-size:1.3rem; font-weight:800; color:#2dd4bf;">${requestedSessions.length}</div>
                <small style="color:var(--text-muted);">Sessions As Learner</small>
            </div>
            <div style="background:rgba(255,255,255,0.03); border:1px solid var(--border-color); border-radius:8px; padding:12px; text-align:center;">
                <div style="font-size:1.3rem; font-weight:800; color:#34d399;">${providedSessions.length}</div>
                <small style="color:var(--text-muted);">Sessions Provided</small>
            </div>
        </div>

        <h5 style="margin-bottom:8px; font-weight:700;"><i class="fa-solid fa-graduation-cap"></i> Offered Skills:</h5>
        ${offeredSkills.length === 0 ? '<p class="text-muted" style="font-size:0.82rem;">None yet.</p>' : `
            <ul style="padding-left:18px; margin-bottom:16px; font-size:0.85rem;">
                ${offeredSkills.map(s => `<li><strong>${escapeHtml(s.skillName)}</strong> (${s.hoursAvailable} hrs avail.) - <em>${escapeHtml(s.category)}</em></li>`).join('')}
            </ul>
        `}

        <button class="btn btn-secondary btn-sm" onclick="closeModal('modal-member-details'); viewMemberLedger(${member.id})">
            <i class="fa-solid fa-receipt"></i> View Full Credit Ledger History
        </button>
    `;
    openModal('modal-member-details');
}

// ─── 5. Skill Offers ───────────────────────────────────────────────────────
async function fetchSkills() {
    try {
        const res = await fetch('/api/skills');
        if (!res.ok) return;
        skillsCache = await res.json();
        renderSkillsCards(skillsCache);
    } catch (err) {
        console.error('Error fetching skills:', err);
    }
}

function renderSkillsCards(skills) {
    const container = document.getElementById('skills-cards-container');
    if (!skills.length) {
        container.innerHTML = '<div class="text-center text-muted" style="grid-column: 1/-1; padding: 40px;">No skill offers available yet. Click "Offer a Skill" or "Load Sample Data" above!</div>';
        return;
    }

    container.innerHTML = skills.map(s => `
        <div class="skill-card">
            <div>
                <div class="skill-card-header">
                    <div>
                        <h4 class="skill-title">${escapeHtml(s.skillName)}</h4>
                        <span class="skill-provider">
                            <i class="fa-solid fa-user"></i> By ${escapeHtml(s.provider?.name || 'Member')}
                        </span>
                    </div>
                    <span class="badge badge-category">${escapeHtml(s.category || 'Technology')}</span>
                </div>
                <p class="skill-desc">${escapeHtml(s.description || 'No description provided.')}</p>
            </div>
            <div class="skill-card-footer">
                <span class="skill-hours-badge"><i class="fa-regular fa-clock"></i> ${s.hoursAvailable} hrs avail.</span>
                <button class="btn btn-primary btn-sm" onclick="quickBookSkill(${s.id})">
                    <i class="fa-solid fa-calendar-plus"></i> Request
                </button>
            </div>
        </div>
    `).join('');
}

function filterSkills() {
    const q = document.getElementById('skill-search').value.toLowerCase();
    const cat = document.getElementById('skill-category-filter').value;

    const filtered = skillsCache.filter(s => {
        const matchesQ = s.skillName.toLowerCase().includes(q) || 
                         (s.description && s.description.toLowerCase().includes(q)) ||
                         (s.provider && s.provider.name.toLowerCase().includes(q));
        const matchesCat = !cat || s.category === cat;
        return matchesQ && matchesCat;
    });
    renderSkillsCards(filtered);
}

async function handleCreateSkill(e) {
    e.preventDefault();
    const skillName = document.getElementById('skill-name').value.trim();
    const providerId = document.getElementById('skill-provider').value;
    const category = document.getElementById('skill-category').value;
    const hoursAvailable = parseInt(document.getElementById('skill-hours').value, 10);
    const description = document.getElementById('skill-desc').value.trim();

    try {
        const res = await fetch('/api/skills', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ skillName, providerId, category, hoursAvailable, description })
        });
        const data = await res.json();
        if (res.ok) {
            showToast(`Skill '${data.skillName}' posted successfully!`, 'success');
            closeModal('modal-skill');
            document.getElementById('form-skill').reset();
            loadAllData();
        } else {
            showToast(data.message || data.error || 'Failed to post skill offer', 'error');
        }
    } catch (err) {
        showToast('Error publishing skill offer', 'error');
    }
}

// ─── 6. Session Requests & Workflow ────────────────────────────────────────
async function fetchSessions() {
    try {
        const res = await fetch('/api/requests');
        if (!res.ok) return;
        sessionsCache = await res.json();
        renderSessionsTable(sessionsCache);
    } catch (err) {
        console.error('Error fetching sessions:', err);
    }
}

function renderSessionsTable(sessions) {
    const tbody = document.getElementById('tbody-sessions');
    if (!sessions.length) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">No session requests found.</td></tr>';
        return;
    }

    tbody.innerHTML = sessions.map(s => {
        let actionButtons = '';
        if (s.status === 'PENDING') {
            actionButtons = `
                <button class="btn btn-primary btn-sm" onclick="handleConfirmSession(${s.id})" title="Provider accepts request">
                    <i class="fa-solid fa-check"></i> Confirm
                </button>
                <button class="btn btn-secondary btn-sm" onclick="handleRejectSession(${s.id})" title="Decline request">
                    <i class="fa-solid fa-xmark"></i> Reject
                </button>
            `;
        } else if (s.status === 'CONFIRMED') {
            actionButtons = `
                <button class="btn btn-success btn-sm" onclick="openCompleteModal(${s.id}, '${escapeHtml(s.skillOffer?.skillName || '')}', '${escapeHtml(s.requester?.name || '')}', '${escapeHtml(s.skillOffer?.provider?.name || '')}', ${s.hoursRequested})" title="Transfer time credits">
                    <i class="fa-solid fa-circle-check"></i> Complete
                </button>
                <button class="btn btn-secondary btn-sm" onclick="handleCancelSession(${s.id})" title="Cancel session">
                    Cancel
                </button>
            `;
        } else {
            actionButtons = `<span class="text-muted"><i class="fa-solid fa-lock"></i> Finalized</span>`;
        }

        return `
            <tr>
                <td>#${s.id}</td>
                <td><strong>${escapeHtml(s.skillOffer?.skillName || 'N/A')}</strong></td>
                <td>${escapeHtml(s.requester?.name || 'N/A')}</td>
                <td>${escapeHtml(s.skillOffer?.provider?.name || 'N/A')}</td>
                <td>${s.hoursRequested} hrs</td>
                <td>${s.actualHoursDelivered ? s.actualHoursDelivered + ' hrs' : '-'}</td>
                <td><span class="badge badge-status-${s.status.toLowerCase()}">${s.status}</span></td>
                <td><div style="display:flex; gap:6px;">${actionButtons}</div></td>
            </tr>
        `;
    }).join('');
}

function filterSessionsByStatus(status, tabElem) {
    if (tabElem) {
        document.querySelectorAll('.status-tab').forEach(t => t.classList.remove('active'));
        tabElem.classList.add('active');
    }

    if (status === 'ALL') {
        renderSessionsTable(sessionsCache);
    } else if (status === 'REJECTED') {
        renderSessionsTable(sessionsCache.filter(s => s.status === 'REJECTED' || s.status === 'CANCELLED'));
    } else {
        renderSessionsTable(sessionsCache.filter(s => s.status === status));
    }
}

async function handleCreateSession(e) {
    e.preventDefault();
    const requesterId = document.getElementById('session-requester').value;
    const skillOfferId = document.getElementById('session-skill').value;
    const hoursRequested = parseInt(document.getElementById('session-hours').value, 10);

    try {
        const res = await fetch('/api/requests', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ requesterId, skillOfferId, hoursRequested })
        });
        const data = await res.json();
        if (res.ok) {
            showToast(`Session request #${data.id} created! (Status: PENDING)`, 'success');
            closeModal('modal-session');
            document.getElementById('form-session').reset();
            loadAllData();
            switchTab('sessions');
        } else {
            showToast(data.message || data.error || 'Failed to create request', 'error');
        }
    } catch (err) {
        showToast('Error submitting session request', 'error');
    }
}

async function handleConfirmSession(id) {
    try {
        const res = await fetch(`/api/requests/${id}/confirm`, { method: 'PUT' });
        const data = await res.json();
        if (res.ok) {
            showToast(`Session #${id} CONFIRMED by provider!`, 'success');
            loadAllData();
        } else {
            showToast(data.message || 'Cannot confirm request', 'error');
        }
    } catch (err) {
        showToast('Error confirming session', 'error');
    }
}

function openCompleteModal(id, skill, requester, provider, requestedHours) {
    document.getElementById('complete-session-id').value = id;
    document.getElementById('complete-actual-hours').value = requestedHours;
    document.getElementById('complete-summary-info').innerHTML = `
        <div><strong>Skill:</strong> ${skill}</div>
        <div><strong>Requester (Paying Credits):</strong> ${requester}</div>
        <div><strong>Provider (Receiving Credits):</strong> ${provider}</div>
        <div><strong>Requested Hours:</strong> ${requestedHours} hrs</div>
    `;
    openModal('modal-complete');
}

async function handleCompleteSession(e) {
    e.preventDefault();
    const id = document.getElementById('complete-session-id').value;
    const actualHours = document.getElementById('complete-actual-hours').value;

    try {
        const res = await fetch(`/api/requests/${id}/complete?actualHours=${actualHours}`, { method: 'PUT' });
        const data = await res.json();
        if (res.ok) {
            showToast(`Session #${id} COMPLETED! ${actualHours} time credits transferred!`, 'success');
            closeModal('modal-complete');
            loadAllData();
        } else {
            showToast(data.message || data.error || 'Cannot complete session', 'error');
        }
    } catch (err) {
        showToast('Error completing session', 'error');
    }
}

async function handleRejectSession(id) {
    if (!confirm('Reject this session request?')) return;
    try {
        const res = await fetch(`/api/requests/${id}/reject`, { method: 'PUT' });
        if (res.ok) {
            showToast(`Session #${id} marked REJECTED`, 'info');
            loadAllData();
        }
    } catch (err) {
        showToast('Error rejecting session', 'error');
    }
}

async function handleCancelSession(id) {
    if (!confirm('Cancel this session request?')) return;
    try {
        const res = await fetch(`/api/requests/${id}/cancel`, { method: 'PUT' });
        if (res.ok) {
            showToast(`Session #${id} CANCELLED`, 'info');
            loadAllData();
        }
    } catch (err) {
        showToast('Error cancelling session', 'error');
    }
}

// ─── 7. Credit Ledger Table ────────────────────────────────────────────────
async function fetchLedger() {
    try {
        const res = await fetch('/api/credits');
        if (!res.ok) return;
        ledgerCache = await res.json();
        renderLedgerTable(ledgerCache);

        // Sum total transferred hours
        const totalHours = ledgerCache.reduce((sum, item) => sum + (item.hours || 0), 0);
        document.getElementById('ledger-total-hours').innerText = totalHours;
    } catch (err) {
        console.error('Error fetching ledger:', err);
    }
}

function renderLedgerTable(items) {
    const tbody = document.getElementById('tbody-ledger');
    if (!items.length) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">No credit transactions recorded yet. Complete a session to generate transactions.</td></tr>';
        return;
    }

    tbody.innerHTML = items.map(l => `
        <tr>
            <td><strong>#TXN-${l.id}</strong></td>
            <td>
                <span style="color:#ef4444;"><i class="fa-solid fa-minus"></i> ${escapeHtml(l.requester?.name || 'Requester')}</span>
                ${l.requesterBalanceAfter != null ? `<small class="text-muted"> (bal: ${l.requesterBalanceAfter})</small>` : ''}
            </td>
            <td>
                <span style="color:#10b981;"><i class="fa-solid fa-plus"></i> ${escapeHtml(l.provider?.name || 'Provider')}</span>
                ${l.providerBalanceAfter != null ? `<small class="text-muted"> (bal: ${l.providerBalanceAfter})</small>` : ''}
            </td>
            <td><span class="badge badge-credit">${l.hours} hrs</span></td>
            <td><span class="badge badge-category">${escapeHtml(l.transactionType || 'EXCHANGE')}</span></td>
            <td>${escapeHtml(l.description || 'Skill exchange session completion')}</td>
            <td><small class="text-muted">${formatDate(l.createdAt)}</small></td>
        </tr>
    `).join('');
}

function filterLedgerTable() {
    const q = document.getElementById('ledger-search').value.toLowerCase();
    const filtered = ledgerCache.filter(l => 
        (l.requester && l.requester.name.toLowerCase().includes(q)) ||
        (l.provider && l.provider.name.toLowerCase().includes(q)) ||
        (l.description && l.description.toLowerCase().includes(q))
    );
    renderLedgerTable(filtered);
}

async function viewMemberLedger(memberId) {
    switchTab('ledger');
    const member = membersCache.find(m => m.id === memberId);
    if (member) {
        document.getElementById('ledger-search').value = member.name;
        filterLedgerTable();
    }
}

// ─── 8. Live REST APIs Console Execution ───────────────────────────────────
async function executeApi(method, url, body, targetId) {
    const pre = document.getElementById(targetId);
    if (!pre) return;
    pre.innerText = 'Calling API...';

    const startTime = performance.now();
    try {
        const options = {
            method: method,
            headers: { 'Content-Type': 'application/json' }
        };
        if (body) {
            options.body = typeof body === 'string' ? body : JSON.stringify(body);
        }

        const res = await fetch(url, options);
        const duration = Math.round(performance.now() - startTime);

        let data;
        const text = await res.text();
        try {
            data = JSON.parse(text);
        } catch {
            data = text;
        }

        const statusLine = `HTTP ${res.status} ${res.statusText} (${duration} ms)\n\n`;
        pre.innerText = statusLine + JSON.stringify(data, null, 2);
    } catch (err) {
        pre.innerText = `Network / Connection Error: ${err.message}`;
    }
}

function executeApiWithBody(method, url, inputId, targetId) {
    const input = document.getElementById(inputId);
    const bodyText = input ? input.value : null;
    executeApi(method, url, bodyText, targetId);
}

// ─── Modal Helpers & Client-Side Credit Check ──────────────────────────────
function openModal(id) {
    document.getElementById(id).classList.add('active');
    populateDropdowns();
}

function closeModal(id) {
    document.getElementById(id).classList.remove('active');
}

function populateDropdowns() {
    // Populate provider select in Skill form
    const selProvider = document.getElementById('skill-provider');
    if (selProvider) {
        selProvider.innerHTML = '<option value="">Select offering member...</option>' +
            membersCache.map(m => `<option value="${m.id}">${escapeHtml(m.name)} (Balance: ${m.creditBalance} hrs)</option>`).join('');
    }

    // Populate requester select in Session form
    const selRequester = document.getElementById('session-requester');
    if (selRequester) {
        selRequester.innerHTML = '<option value="">Select requester...</option>' +
            membersCache.map(m => `<option value="${m.id}" data-balance="${m.creditBalance}">${escapeHtml(m.name)} (Balance: ${m.creditBalance} hrs)</option>`).join('');
    }

    // Populate skill select in Session form
    const selSkill = document.getElementById('session-skill');
    if (selSkill) {
        selSkill.innerHTML = '<option value="">Select skill offer...</option>' +
            skillsCache.map(s => `<option value="${s.id}">${escapeHtml(s.skillName)} — Provider: ${escapeHtml(s.provider?.name || 'Member')} (${s.hoursAvailable} hrs avail.)</option>`).join('');
    }
}

function checkRequesterCredit() {
    const selRequester = document.getElementById('session-requester');
    const opt = selRequester.options[selRequester.selectedIndex];
    const hint = document.getElementById('requester-credit-hint');
    const warningBox = document.getElementById('credit-warning-box');
    const warningMsg = document.getElementById('credit-warning-msg');
    const submitBtn = document.getElementById('btn-submit-session');
    const hours = parseInt(document.getElementById('session-hours').value, 10) || 0;

    if (!opt || !opt.dataset.balance) {
        hint.innerText = '';
        warningBox.style.display = 'none';
        submitBtn.disabled = false;
        return;
    }

    const balance = parseInt(opt.dataset.balance, 10);
    hint.innerText = `Current available balance: ${balance} credits`;

    // Business Rule 1 check on UI
    if (balance < hours) {
        warningBox.style.display = 'flex';
        warningMsg.innerText = `Insufficient credits! Requester only has ${balance} credits, but needs ${hours}.`;
        submitBtn.disabled = true;
    } else {
        warningBox.style.display = 'none';
        submitBtn.disabled = false;
    }
}

function quickBookSkill(skillId) {
    openModal('modal-session');
    const selSkill = document.getElementById('session-skill');
    if (selSkill) {
        selSkill.value = skillId;
    }
}

// ─── Toast Notifications ───────────────────────────────────────────────────
function showToast(msg, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;

    let icon = 'fa-info-circle';
    if (type === 'success') icon = 'fa-check-circle';
    if (type === 'error') icon = 'fa-triangle-exclamation';

    toast.innerHTML = `<i class="fa-solid ${icon}"></i> <span>${escapeHtml(msg)}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Utilities
function escapeHtml(text) {
    if (!text) return '';
    return String(text).replace(/[&<>"']/g, m => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;'
    })[m]);
}

function formatDate(isoStr) {
    if (!isoStr) return '-';
    try {
        const d = new Date(isoStr);
        return d.toLocaleDateString() + ' ' + d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } catch {
        return isoStr;
    }
}
