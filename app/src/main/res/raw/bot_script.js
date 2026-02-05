(function() {
    if (window.botInjected) return;
    window.botInjected = true;
    window.Android.log("Bot Script Injected Successfully");

    window.targetMultiplier = 1.20;
    window.botEnabled = false;

    // Helper to find elements by text (case insensitive)
    function findElementByText(selector, text) {
        const elements = document.querySelectorAll(selector);
        for (let el of elements) {
            if (el.innerText && el.innerText.toLowerCase().includes(text.toLowerCase())) {
                // Ensure it's visible
                if (el.offsetWidth > 0 && el.offsetHeight > 0) {
                    return el;
                }
            }
        }
        return null;
    }

    function scanAndAct() {
        if (!window.botEnabled) return;

        // --- 1. Detect Multiplier (Game State) ---
        let currentMultiplier = 0.0;
        let possibleMultipliers = Array.from(document.querySelectorAll('div, span, p')).filter(el => {
            return el.innerText && /^\d+\.\d+x?$/.test(el.innerText) && el.offsetHeight > 20;
        });

        // Sort by font size (largest is likely the main counter)
        possibleMultipliers.sort((a, b) => {
            let sA = parseFloat(window.getComputedStyle(a).fontSize);
            let sB = parseFloat(window.getComputedStyle(b).fontSize);
            return sB - sA;
        });

        if (possibleMultipliers.length > 0) {
            let text = possibleMultipliers[0].innerText.replace('x', '');
            currentMultiplier = parseFloat(text);
            if (!isNaN(currentMultiplier)) {
                window.Android.onMultiplier(currentMultiplier.toFixed(2));
            }
        }

        // --- 2. Action Logic ---

        let cashOutBtn = findElementByText('button', 'Cash Out');
        if (!cashOutBtn) cashOutBtn = findElementByText('div[role="button"]', 'Cash Out');
        if (!cashOutBtn) cashOutBtn = findElementByText('button', 'Cashout');

        if (cashOutBtn) {
            if (currentMultiplier >= window.targetMultiplier) {
                cashOutBtn.click();
                window.Android.log("CASH OUT CLICKED at " + currentMultiplier + "x (Target: " + window.targetMultiplier + ")");
            }
            return;
        }

        let cancelBtn = findElementByText('button', 'Cancel');
        if (cancelBtn) {
            // Waiting...
            // Perfect time to scrape history since the round ended recently
            if (Math.random() < 0.2) scrapeAndSendHistory();
            return;
        }

        let betBtn = findElementByText('button', 'Bet');
        if (!betBtn) betBtn = findElementByText('div[role="button"]', 'Bet');

        if (betBtn) {
            betBtn.click();
            window.Android.log("PLACING BET");
        }

        // Also scrape occasionally during game just in case
        if (Math.random() < 0.05) scrapeAndSendHistory();
    }

    function scrapeAndSendHistory() {
        // Scrape history pills (small colored blocks with numbers)
        // Heuristic: Elements with text like "1.23x" grouped together.

        let pills = Array.from(document.querySelectorAll('div, span, button')).filter(el => {
            // Must match format 1.23x, visible, and not huge (huge is main counter)
            return el.innerText && /^\d+\.\d+x$/.test(el.innerText) &&
                   el.offsetHeight > 10 && el.offsetHeight < 50;
        });

        // Filter by font size < 30px (Main counter is usually much larger)
        pills = pills.filter(p => {
             let fontSize = parseFloat(window.getComputedStyle(p).fontSize);
             return fontSize < 30;
        });

        if (pills.length > 0) {
            // Extract values
            let values = pills.map(p => p.innerText.replace('x', '').trim());
            // Join
            let historyStr = values.join(",");
            window.Android.sendHistory(historyStr);
        }
    }

    // Scan frequently
    setInterval(scanAndAct, 200);

    // --- External Control ---
    window.updateBotParams = function(enabled, target) {
        window.botEnabled = enabled;
        window.targetMultiplier = parseFloat(target);
        window.Android.log("JS Params: Enabled=" + enabled + ", Target=" + target);
    };

})();
