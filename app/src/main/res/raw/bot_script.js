(function() {
    console.log("AviatorBot: JS Injected");
    Android.logFromJs("JS Injected and running");

    function findElementByText(tag, text) {
        const elements = document.getElementsByTagName(tag);
        for (let i = 0; i < elements.length; i++) {
            if (elements[i].textContent.includes(text)) {
                return elements[i];
            }
        }
        return null;
    }

    let lastMultiplier = "";

    function loop() {
        try {
            // Attempt to find the main multiplier number
            let multiplierEl = document.querySelector('.payout');
            if (!multiplierEl) multiplierEl = document.querySelector('.bubble-multiplier');

            if (multiplierEl) {
                let currentText = multiplierEl.innerText.trim();

                // Update Android if changed
                if (currentText !== lastMultiplier) {

                    // Detect Game Start (Reset to 1.00x)
                    if (currentText === "1.00x" || currentText === "1.0x") {
                        Android.gameStart();
                        Android.logFromJs("Detected Game Start");
                    }

                    lastMultiplier = currentText;
                    if (currentText.includes('x')) {
                        Android.updateMultiplier(currentText);
                    }
                }

                // Check for Crash state
                if (document.body.innerText.includes('Flew Away')) {
                     Android.gameCrash(currentText);
                }
            }

        } catch (e) {
            // Android.logFromJs("Error in loop: " + e.message);
        }
    }

    setInterval(loop, 200);

    // Global function for Android to call
    window.clickButton = function(action) {
        try {
            Android.logFromJs("Attempting to click: " + action);
            let btn = null;

            if (action === "BET") {
                // Heuristic: Look for "BET" text
                btn = findElementByText('button', 'BET');
                if (!btn) btn = findElementByText('div', 'BET');
            } else if (action === "CASHOUT") {
                // Heuristic: Look for "CASH OUT" text
                btn = findElementByText('button', 'CASH OUT');
                if (!btn) btn = findElementByText('div', 'CASH OUT');
            }

            if (btn) {
                btn.click();
                Android.logFromJs("Clicked " + action);
            } else {
                Android.logFromJs("Button not found for " + action);
            }
        } catch(e) {
            Android.logFromJs("Error clicking: " + e.message);
        }
    };

})();
